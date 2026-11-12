package it.bonsaigreenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.List;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Server-authoritative crops; 3 inputs and 9 automatic output slots. */
public class GreenhouseBlockEntity extends BlockEntity implements WorldlyContainer, net.minecraft.world.MenuProvider {
    // Slots 0 soil, 1 seed, 2 bone meal; 9..17 produced items.
    // Preserve the 27-slot serialized layout for existing alpha worlds.
    private final NonNullList<ItemStack> stacks=NonNullList.withSize(27,ItemStack.EMPTY);
    private final NonNullList<ItemStack> pending=NonNullList.withSize(9,ItemStack.EMPTY);
    private static final int[] TOP={0,1,2};
    private static final int[] SIDE={2};
    private static final int[] BOTTOM={9,10,11,12,13,14,15,16,17};
    private static final int[] NONE={};
    /**
     * The normal greenhouse keeps growing until it holds this many ready
     * harvests, collected all at once with "Harvest". Finished harvests wait in
     * the nine internal slots 9..17 (not shown, not reachable by automation);
     * the last one stays pending. The automatic greenhouse exports instead.
     */
    public static final int NORMAL_HARVESTS=4;
    private int storedHarvests=0;
    /** Ready harvests of the normal greenhouse, 0..NORMAL_HARVESTS (shown as "n/4"). */
    public int readyHarvests() { return storedHarvests + (mature ? 1 : 0); }
    private Crop active=Crop.EMPTY;
    private int elapsed=0, duration=0, chargedCycles=0;
    private boolean mature=false;
    private boolean isReadyForNewCycle=true;
    private int transferCooldown=0;
    // Redstone: a powered greenhouse pauses growth. The state is re-read from
    // the world after loading and whenever a neighbor changes, never saved.
    private boolean powered=false, powerChecked=false;
    // Last comparator value sent to neighbors; -1 until the first server tick.
    private int lastSignal=-1;

    public GreenhouseBlockEntity(BlockPos pos, BlockState state) { super(BonsaiGreenhouse.GREENHOUSE_ENTITY.get(),pos,state); }
    /** Crop currently growing or holding a ready harvest (EMPTY if none). */
    public Crop activeCrop() { return active; }
    /** Whole seconds left in a cycle at 20 TPS, rounded up; shared by the GUI and Jade. */
    public static int remainingSeconds(int elapsed, int duration) {
        return Math.max(0, (duration - elapsed + 19) / 20);
    }
    /** Called by the block when a neighbor changes: re-read redstone power next tick. */
    public void markPowerDirty() { powerChecked=false; }
    public boolean isPowered() { return powered; }
    /**
     * Comparator output. Normal greenhouse: 0 without a growing crop, 1-14
     * while growing (proportional to progress), 15 when its three outputs are
     * full and a harvest is waiting for "Harvest".
     * Automatic greenhouse: fill level of the nine output slots, like a chest.
     */
    public int comparatorSignal() {
        if (automatic()) {
            float fill=0;
            for (int i=9;i<18;i++) {
                ItemStack s=stacks.get(i);
                if (!s.isEmpty()) fill+=(float)s.getCount()/Math.min(getMaxStackSize(),s.getMaxStackSize());
            }
            return fill<=0 ? 0 : Math.min(15,(int)Math.floor(fill/9f*14f)+1);
        }
        if (mature) return 15;  // normal: all four harvests ready (or no room left)
        if (active==Crop.EMPTY || duration<=0) return 0;
        return Math.min(14,1+(elapsed*14)/duration);
    }
    private boolean automatic() { return getBlockState().getBlock() == BonsaiGreenhouse.HOPPING.get(); }
    public boolean isAutomatic() { return automatic(); }
    public net.minecraft.world.inventory.ContainerData dataAccess() {
        return new net.minecraft.world.inventory.ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> elapsed;
                    case 1 -> duration;
                    case 2 -> mature ? 1 : 0;
                    case 3 -> chargedCycles;
                    case 4 -> powered ? 1 : 0;
                    case 5 -> readyHarvests();
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 6; }
        };
    }
    private Crop selected() {
        Crop crop=Crop.of(stacks.get(1));
        if (crop==Crop.EMPTY || !crop.accepts(stacks.get(0))) return Crop.EMPTY;
        if (crop==Crop.POPLAR_RED) {
            // Vanilla has one sapling and three equally likely leaf colors.
            // Choose only when establishing a tree; retain its serialized
            // color through subsequent ticks, harvests and world reloads.
            if (active.isPoplar()) return active;
            return switch(level.getRandom().nextInt(3)) {
                case 0 -> Crop.POPLAR_RED;
                case 1 -> Crop.POPLAR_ORANGE;
                default -> Crop.POPLAR_YELLOW;
            };
        }
        return crop;
    }
    /**
     * A ready harvest belongs to the crop that produced it. Keep it, and the
     * crop it came from, until it is collected or exported, even if the plant
     * or substrate is removed or replaced in the meantime. This also covers
     * retired legacy crops (moss, weeping vines on nether wart blocks).
     */
    private boolean hasReadyHarvest() {
        if (!mature) return false;
        for (ItemStack item : pending) if (!item.isEmpty()) return true;
        return false;
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, GreenhouseBlockEntity be) {
        Crop selected=be.selected();
        if (selected!=be.active && !be.hasReadyHarvest()) {
            // An interrupted accelerated cycle was already deducted from the
            // bone meal charge: give it back so one bone meal still yields
            // four accelerated harvests.
            if (!be.mature && be.duration>0 && be.duration<be.active.duration)
                be.chargedCycles=Math.min(4,be.chargedCycles+1);
            be.active=selected;
            be.elapsed=0; be.duration=0; be.mature=false;
            be.isReadyForNewCycle=true; be.pending.replaceAll(x -> ItemStack.EMPTY);
            be.visualSync(); be.setChanged();
        }
        if (be.needsVisualSync) { be.needsVisualSync=false; be.visualSync(); }
        if (!be.powerChecked) {
            be.powered=level.hasNeighborSignal(pos);
            be.powerChecked=true;
        }
        if (be.automatic() && ++be.transferCooldown>=8) {
            be.transferCooldown=0;
            be.pushDown(level,pos);
        }
        // Redstone pauses growth only: no new cycle starts and no bone meal is
        // spent, while a ready harvest can still be collected or exported.
        if (selected!=Crop.EMPTY && !be.mature && !be.powered) {
            if (be.isReadyForNewCycle) be.startCycle();
            if (++be.elapsed >= be.duration) {
                be.elapsed=be.duration;
                be.mature=true;
                List<ItemStack> rolled=selected.roll(level.getRandom());
                for (int i=0;i<be.pending.size();i++) be.pending.set(i, i<rolled.size() ? rolled.get(i) : ItemStack.EMPTY);
                be.setChanged();
            }
            // Persist every growth tick without sending comparator updates each
            // tick. setChanged() remains appropriate for inventory transitions.
            level.blockEntityChanged(pos);
            be.visualSync();
        }
        // Automatic: move a ready harvest into the outputs and start again,
        // or wait for space. Normal: keep up to four ready harvests; the
        // fourth (or one that no longer fits) waits for "Harvest".
        if (be.mature && (be.automatic() || be.storedHarvests<NORMAL_HARVESTS-1) && be.canStorePending()) {
            be.storePending();
            if (!be.automatic()) be.storedHarvests++;
            be.resetCycle();
        }
        // Notify comparators only when the value actually changes, so growth
        // ticks stay cheap and an idle machine sends no updates.
        int signal=be.comparatorSignal();
        if (be.lastSignal<0) be.lastSignal=signal;
        else if (signal!=be.lastSignal) {
            be.lastSignal=signal;
            level.updateNeighbourForOutputSignal(pos,state.getBlock());
        }
    }
    private void startCycle() {
        boolean accelerated=chargedCycles>0;
        if (!accelerated && stacks.get(2).is(Items.BONE_MEAL)) {
            stacks.get(2).shrink(1);
            chargedCycles=4; accelerated=true;
        }
        if (accelerated) chargedCycles--;
        duration=accelerated ? active.duration/2 : active.duration;
        elapsed=0;
        isReadyForNewCycle=false;
        setChanged();
    }
    private void resetCycle() {
        mature=false; elapsed=0; duration=0; isReadyForNewCycle=true;
        pending.replaceAll(x->ItemStack.EMPTY);
        visualSync(); setChanged();
    }
    /** "Harvest" on the normal greenhouse: hands over every ready harvest at once. */
    public void collectBy(Player player) {
        if (automatic() || level==null || level.isClientSide()) return;
        boolean changed=false;
        for (int i=9;i<18;i++) {
            ItemStack stack=stacks.get(i);
            if (stack.isEmpty()) continue;
            give(player,stack.copy());
            stacks.set(i,ItemStack.EMPTY);
            changed=true;
        }
        if (mature) {
            for (ItemStack stack : pending) if (!stack.isEmpty()) give(player,stack.copy());
            resetCycle();
            changed=true;
        }
        storedHarvests=0;
        if (changed) setChanged();
    }
    private void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) net.minecraft.world.level.block.Block.popResource(level,worldPosition,stack);
        else if (!stack.isEmpty()) net.minecraft.world.level.block.Block.popResource(level,worldPosition,stack);
    }
    /** True when the normal greenhouse has something for the "Harvest" button. */
    public boolean hasHarvest() {
        if (mature) return true;
        for (int i=9;i<18;i++) if (!stacks.get(i).isEmpty()) return true;
        return false;
    }
    private boolean canStorePending() {
        int outputs=9;
        NonNullList<ItemStack> simulated=NonNullList.withSize(outputs,ItemStack.EMPTY);
        for (int i=0;i<outputs;i++) simulated.set(i,stacks.get(9+i).copy());
        for (ItemStack item : pending) if (!item.isEmpty() && !mergeInto(simulated,item.copy())) return false;
        return true;
    }
    private void storePending() {
        int outputs=9;
        NonNullList<ItemStack> simulated=NonNullList.withSize(outputs,ItemStack.EMPTY);
        for (int i=0;i<outputs;i++) simulated.set(i,stacks.get(9+i).copy());
        for (ItemStack item : pending) if (!item.isEmpty()) mergeInto(simulated,item.copy());
        for (int i=0;i<outputs;i++) stacks.set(9+i,simulated.get(i));
        setChanged();
    }
    /** Transactional stack merge, returns false if one item cannot fit. */
    private static boolean mergeInto(NonNullList<ItemStack> target, ItemStack incoming) {
        for (int i=0;i<target.size();i++) {
            ItemStack current=target.get(i);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current,incoming)) {
                int move=Math.min(incoming.getCount(),current.getMaxStackSize()-current.getCount());
                if (move>0) {current.grow(move);incoming.shrink(move);}
                if (incoming.isEmpty()) return true;
            }
        }
        for (int i=0;i<target.size();i++) if (target.get(i).isEmpty()) {
            int move=Math.min(incoming.getCount(),incoming.getMaxStackSize());
            target.set(i,incoming.copyWithCount(move));incoming.shrink(move);
            if (incoming.isEmpty()) return true;
        }
        return false;
    }
    /**
     * Export into a storage block from another mod (drawers, crates, machines)
     * that is not a vanilla Container but exposes NeoForge's item handler.
     * The insertion is simulated in a transaction and only committed amounts
     * are removed from the outputs, so no item can be lost or duplicated.
     */
    private void pushToItemHandler(Level level, BlockPos belowPos) {
        ResourceHandler<ItemResource> handler=level.getCapability(Capabilities.Item.BLOCK,belowPos,Direction.UP);
        if (handler==null) return;
        int[] moved=new int[9];
        boolean any=false;
        try (Transaction tx=Transaction.openRoot()) {
            for (int i=0;i<9;i++) {
                ItemStack source=stacks.get(9+i);
                if (source.isEmpty()) continue;
                moved[i]=ResourceHandlerUtil.insertStacking(handler,ItemResource.of(source),source.getCount(),tx);
                if (moved[i]>0) any=true;
            }
            if (!any) return;
            tx.commit();
        }
        for (int i=0;i<9;i++) if (moved[i]>0) stacks.get(9+i).shrink(moved[i]);
        setChanged();
    }
    private void pushDown(Level level, BlockPos pos) {
        boolean hasOutput=false;
        for (int i=9;i<18;i++) if (!stacks.get(i).isEmpty()) { hasOutput=true; break; }
        if (!hasOutput) return;
        BlockPos belowPos=pos.below();
        BlockState belowState=level.getBlockState(belowPos);
        Container target;
        if (belowState.getBlock() instanceof WorldlyContainerHolder holder)
            target=holder.getContainer(belowState,level,belowPos);
        else if (level.getBlockEntity(belowPos) instanceof Container container)
            target=container;
        else { pushToItemHandler(level,belowPos); return; }
        if (target instanceof ChestBlockEntity && belowState.getBlock() instanceof ChestBlock chest)
            target=ChestBlock.getContainer(chest,belowState,level,belowPos,true);
        if (target==null || target==this) return;
        int[] destinations;
        if (target instanceof WorldlyContainer sided) destinations=sided.getSlotsForFace(Direction.UP);
        else {
            destinations=new int[target.getContainerSize()];
            for (int i=0;i<destinations.length;i++) destinations[i]=i;
        }
        boolean changed=false;
        for (int i=9;i<18;i++) {
            ItemStack source=stacks.get(i);
            if (source.isEmpty()) continue;
            for (int dest : destinations) {
                if (source.isEmpty()) break;
                if (dest<0 || dest>=target.getContainerSize()) continue;
                if (!target.canPlaceItem(dest,source)) continue;
                if (target instanceof WorldlyContainer sided && !sided.canPlaceItemThroughFace(dest,source,Direction.UP)) continue;
                ItemStack targetStack=target.getItem(dest);
                int limit=target.getMaxStackSize(source);
                if (targetStack.isEmpty()) {
                    int count=Math.min(source.getCount(),limit);
                    if (count>0) { target.setItem(dest,source.copyWithCount(count)); source.shrink(count); changed=true; }
                } else if (ItemStack.isSameItemSameComponents(source,targetStack)) {
                    int count=Math.min(source.getCount(),Math.min(limit,targetStack.getMaxStackSize())-targetStack.getCount());
                    if (count>0) { targetStack.grow(count); source.shrink(count); changed=true; }
                }
            }
        }
        if (changed) { target.setChanged(); setChanged(); }
    }
    private void visualSync() {
        if (level==null || level.isClientSide()) return;
        int stage=active==Crop.EMPTY || duration==0 ? 0 : Math.min(3, (elapsed*4)/duration);
        BlockState old=getBlockState();
        if (old.getValue(GreenhouseBlock.CROP)!=active || old.getValue(GreenhouseBlock.STAGE)!=stage)
            level.setBlock(worldPosition,old.setValue(GreenhouseBlock.CROP,active).setValue(GreenhouseBlock.STAGE,stage),3);
    }
    /** Return stored inputs, outputs and any mature harvest when removed. */
    private void dropContentsForBreak(Level level, BlockPos pos) {
        for (ItemStack item : stacks) if (!item.isEmpty())
            net.minecraft.world.level.block.Block.popResource(level,pos,item.copy());
        for (ItemStack item : pending) if (!item.isEmpty())
            net.minecraft.world.level.block.Block.popResource(level,pos,item.copy());
        clearContent();
        pending.replaceAll(x -> ItemStack.EMPTY);
    }
    // ---- Keeping the contents inside the dropped item (like a shulker box) ----
    // Inputs, outputs and a ready harvest are stored in the item's container
    // component (27 machine slots + 9 pending harvest slots); the growth state
    // in its custom data. The loot table copies both from this block entity.
    private static final int PACKED_SLOTS=36;
    private static final String STATE_KEY="greenhouse_state";
    // Some break paths generate loot before removal and others do so afterwards.
    private long packedDropTick=Long.MIN_VALUE;
    private boolean needsVisualSync=false;
    private boolean hasStoredItems() {
        for (ItemStack s : stacks) if (!s.isEmpty()) return true;
        for (ItemStack s : pending) if (!s.isEmpty()) return true;
        return false;
    }
    private boolean hasGrowthState() {
        return duration>0 || mature || chargedCycles>0 || storedHarvests>0
            || (active!=Crop.EMPTY && hasStoredItems());
    }
    boolean hasPackedContents() { return hasStoredItems() || hasGrowthState(); }
    void preserveContentsForDrop() { packedDropTick=level.getGameTime(); }
    void clearPackedDropMark() { packedDropTick=Long.MIN_VALUE; }
    boolean isPackedDrop(ItemStack stack) {
        if (!stack.is(getBlockState().getBlock().asItem())) return false;
        var custom=stack.get(DataComponents.CUSTOM_DATA);
        return stack.has(DataComponents.CONTAINER) || (custom!=null && custom.contains(STATE_KEY));
    }
    @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (hasStoredItems()) {
            NonNullList<ItemStack> all=NonNullList.withSize(PACKED_SLOTS,ItemStack.EMPTY);
            for (int i=0;i<27;i++) all.set(i,stacks.get(i).copy());
            for (int i=0;i<9;i++) all.set(27+i,pending.get(i).copy());
            builder.set(DataComponents.CONTAINER,
                net.minecraft.world.item.component.ItemContainerContents.fromItems(all));
        }
        if (hasGrowthState()) {
            net.minecraft.nbt.CompoundTag state=new net.minecraft.nbt.CompoundTag();
            state.putString("crop",active.getSerializedName());
            state.putInt("elapsed",elapsed); state.putInt("duration",duration);
            state.putInt("chargedCycles",chargedCycles);
            state.putInt("storedHarvests",storedHarvests);
            state.putBoolean("mature",mature); state.putBoolean("newCycle",isReadyForNewCycle);
            net.minecraft.nbt.CompoundTag data=new net.minecraft.nbt.CompoundTag();
            data.put(STATE_KEY,state);
            builder.set(DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(data));
        }
    }
    @Override protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter components) {
        super.applyImplicitComponents(components);
        var contents=components.get(DataComponents.CONTAINER);
        if (contents!=null) {
            NonNullList<ItemStack> all=NonNullList.withSize(PACKED_SLOTS,ItemStack.EMPTY);
            contents.copyInto(all);
            for (int i=0;i<27;i++) stacks.set(i,all.get(i));
            for (int i=0;i<9;i++) pending.set(i,all.get(27+i));
        }
        var custom=components.get(DataComponents.CUSTOM_DATA);
        if (custom!=null) custom.copyTag().getCompound(STATE_KEY).ifPresent(state->{
            try { active=Crop.valueOf(state.getStringOr("crop","empty").toUpperCase(java.util.Locale.ROOT)); }
            catch (IllegalArgumentException ex) { active=Crop.EMPTY; }
            elapsed=state.getIntOr("elapsed",0); duration=state.getIntOr("duration",0);
            chargedCycles=state.getIntOr("chargedCycles",0);
            storedHarvests=state.getIntOr("storedHarvests",0);
            mature=state.getBooleanOr("mature",false);
            isReadyForNewCycle=state.getBooleanOr("newCycle",true);
        });
        // A machine and a manual greenhouse share one block entity type, so a
        // harvest packed from either can be restored into either.
        needsVisualSync=true;
    }
    /** The packed contents live in components; keep them out of block entity data copies. */
    @Override public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("Items"); output.discard("pending");
        for (String key : List.of("crop","elapsed","duration","chargedCycles","storedHarvests","mature","newCycle")) output.discard(key);
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // BlockEntity's default removal drops Container slots loose and drains
        // them. Keep the slots intact for loot, or drop both inventories here.
        if (level!=null && !level.isClientSide()) {
            if (level.getGameTime()!=packedDropTick) dropContentsForBreak(level,pos);
            clearPackedDropMark();
        }
    }
    @Override public int getContainerSize() { return 27; }
    @Override public boolean isEmpty() { for (ItemStack s:stacks) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return stacks.get(slot); }
    @Override public ItemStack removeItem(int slot,int count) { ItemStack s=ContainerHelper.removeItem(stacks,slot,count); if (!s.isEmpty()) setChanged();return s; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(stacks,slot); }
    @Override public void setItem(int slot,ItemStack stack) {
        if (slot<0 || slot>=27 || (slot!=0 && slot!=1 && slot!=2 && !(slot>=9 && slot<18))) return;
        if (!stack.isEmpty() && !canPlaceItem(slot,stack)) return;
        stacks.set(slot,stack);
        if (stack.getCount()>getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean canPlaceItem(int slot,ItemStack stack) {
        if (slot==0) {
            // Allow supported vanilla substrates, including soul sand and nylium.
            return java.util.Arrays.stream(Crop.values()).anyMatch(c->c.accepts(stack));
        }
        if (slot==1) return Crop.of(stack)!=Crop.EMPTY;
        if (slot==2) return stack.is(Items.BONE_MEAL);
        return false;
    }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this,player); }
    @Override public void clearContent() { for (int i=0;i<stacks.size();i++) stacks.set(i,ItemStack.EMPTY); setChanged(); }
    // The normal greenhouse's outputs are emptied by hand only: nothing is extractable from below.
    @Override public int[] getSlotsForFace(Direction face) { return face==Direction.DOWN ? (automatic() ? BOTTOM : NONE) : face==Direction.UP ? TOP : SIDE; }
    /**
     * Automation capacity (vanilla hoppers and NeoForge item handlers): only
     * bone meal stacks; soil and plant slots hold a single item, as in the menu.
     * Output slots are filled by the greenhouse itself, not through this limit.
     */
    @Override public int getMaxStackSize(ItemStack stack) {
        return stack.is(Items.BONE_MEAL) ? Math.min(getMaxStackSize(),stack.getMaxStackSize()) : 1;
    }
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face) {
        // Soil and plant hold a single item, as in the menu: automation may
        // fill an empty slot but never stack extra copies on top.
        if ((slot==0 || slot==1) && !stacks.get(slot).isEmpty()) return false;
        return canPlaceItem(slot,stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction face) { return automatic() && face==Direction.DOWN && slot>=9 && slot<18; }
    @Override public Component getDisplayName() { return Component.translatable(automatic()?"block.greenhouse.automatic_greenhouse":"block.greenhouse.greenhouse"); }
    @Override public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new GreenhouseMenu(windowId,inventory,this);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output,stacks);
        ContainerHelper.saveAllItems(output.child("pending"),pending);
        output.putInt("elapsed",elapsed);output.putInt("duration",duration);
        output.putInt("chargedCycles",chargedCycles);
        output.putInt("storedHarvests",storedHarvests);
        output.putString("crop",active.getSerializedName());
        output.putBoolean("mature",mature); output.putBoolean("newCycle",isReadyForNewCycle);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input,stacks);
        ContainerHelper.loadAllItems(input.childOrEmpty("pending"),pending);
        elapsed=input.getIntOr("elapsed",0);duration=input.getIntOr("duration",0);
        chargedCycles=input.getIntOr("chargedCycles",0);
        storedHarvests=input.getIntOr("storedHarvests",0);
        try { active=Crop.valueOf(input.getStringOr("crop","empty").toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException ex) { active=Crop.EMPTY; }
        mature=input.getBooleanOr("mature",false);
        isReadyForNewCycle=input.getBooleanOr("newCycle",true);
    }
}
