package it.bonsaigreenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.ArrayList;
import java.util.List;

/** Server-authoritative crops; 3 inputs + 9 products exposed through a vanilla GUI. */
public class GreenhouseBlockEntity extends BlockEntity implements WorldlyContainer, net.minecraft.world.MenuProvider {
    // Slots 0 soil, 1 seed, 2 bone meal; 9..17 produced items.
    // Unused slots in the vanilla 3-row chest GUI cannot accept input.
    private final NonNullList<ItemStack> stacks=NonNullList.withSize(27,ItemStack.EMPTY);
    private final NonNullList<ItemStack> pending=NonNullList.withSize(9,ItemStack.EMPTY);
    private static final int[] TOP={0,1,2};
    private static final int[] SIDE={2};
    private static final int[] BOTTOM={9,10,11,12,13,14,15,16,17};
    private Crop active=Crop.EMPTY;
    private int elapsed=0, duration=0, chargedCycles=0;
    private boolean mature=false;
    private boolean isReadyForNewCycle=true;
    private int transferCooldown=0;

    public GreenhouseBlockEntity(BlockPos pos, BlockState state) { super(BonsaiGreenhouse.GREENHOUSE_ENTITY.get(),pos,state); }
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
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 4; }
        };
    }
    private Crop selected() {
        Crop crop=Crop.of(stacks.get(1));
        return crop!=Crop.EMPTY && crop.accepts(stacks.get(0)) ? crop : Crop.EMPTY;
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, GreenhouseBlockEntity be) {
        Crop selected=be.selected();
        if (selected!=be.active) {
            be.active=selected;
            be.elapsed=0; be.duration=0; be.mature=false;
            be.isReadyForNewCycle=true; be.pending.replaceAll(x -> ItemStack.EMPTY);
            be.visualSync(); be.setChanged();
        }
        if (be.automatic() && ++be.transferCooldown>=8) {
            be.transferCooldown=0;
            be.pushDown(level,pos);
        }
        if (selected==Crop.EMPTY) return;
        if (!be.mature) {
            if (be.isReadyForNewCycle) be.startCycle();
            if (++be.elapsed >= be.duration) {
                be.elapsed=be.duration;
                be.mature=true;
                List<ItemStack> rolled=selected.roll(level.getRandom());
                for (int i=0;i<be.pending.size();i++) be.pending.set(i, i<rolled.size() ? rolled.get(i) : ItemStack.EMPTY);
                be.setChanged();
            }
            be.visualSync();
        }
        if (be.automatic() && be.mature && be.canStorePending()) {
            be.storePending();
            be.resetCycle();
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
    public void collectBy(Player player) {
        if (automatic() || !mature || level==null || level.isClientSide()) return;
        for (ItemStack stack : pending) if (!stack.isEmpty()) {
            ItemStack remain=stack.copy();
            if (!player.getInventory().add(remain)) net.minecraft.world.level.block.Block.popResource(level,worldPosition,remain);
            else if (!remain.isEmpty()) net.minecraft.world.level.block.Block.popResource(level,worldPosition,remain);
        }
        resetCycle();
    }
    private boolean canStorePending() {
        NonNullList<ItemStack> simulated=NonNullList.withSize(9,ItemStack.EMPTY);
        for (int i=0;i<9;i++) simulated.set(i,stacks.get(9+i).copy());
        for (ItemStack item : pending) if (!item.isEmpty() && !mergeInto(simulated,item.copy())) return false;
        return true;
    }
    private void storePending() {
        NonNullList<ItemStack> simulated=NonNullList.withSize(9,ItemStack.EMPTY);
        for (int i=0;i<9;i++) simulated.set(i,stacks.get(9+i).copy());
        for (ItemStack item : pending) if (!item.isEmpty()) mergeInto(simulated,item.copy());
        for (int i=0;i<9;i++) stacks.set(9+i,simulated.get(i));
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
    private void pushDown(Level level, BlockPos pos) {
        BlockEntity below=level.getBlockEntity(pos.below());
        if (!(below instanceof Container target) || below==this) return;
        for (int i=9;i<18;i++) {
            ItemStack source=stacks.get(i);
            if (source.isEmpty()) continue;
            for (int dest=0;dest<target.getContainerSize() && !source.isEmpty();dest++) {
                if (!target.canPlaceItem(dest,source)) continue;
                ItemStack targetStack=target.getItem(dest);
                if (targetStack.isEmpty()) {
                    int count=Math.min(source.getCount(),source.getMaxStackSize());
                    target.setItem(dest,source.copyWithCount(count));source.shrink(count);
                } else if (ItemStack.isSameItemSameComponents(source,targetStack)) {
                    int count=Math.min(source.getCount(),targetStack.getMaxStackSize()-targetStack.getCount());
                    if (count>0) { targetStack.grow(count); source.shrink(count); target.setChanged(); }
                }
            }
        }
        setChanged();
    }
    private void visualSync() {
        if (level==null || level.isClientSide()) return;
        int stage=active==Crop.EMPTY || duration==0 ? 0 : Math.min(3, (elapsed*4)/duration);
        BlockState old=getBlockState();
        if (old.getValue(GreenhouseBlock.CROP)!=active || old.getValue(GreenhouseBlock.STAGE)!=stage)
            level.setBlock(worldPosition,old.setValue(GreenhouseBlock.CROP,active).setValue(GreenhouseBlock.STAGE,stage),3);
    }
    /** On player break, return substrates, seeds, bone meal, and nine output stacks. */
    public void dropContentsForBreak(Level level, BlockPos pos) {
        for (ItemStack item : stacks) if (!item.isEmpty())
            net.minecraft.world.level.block.Block.popResource(level,pos,item.copy());
        for (ItemStack item : pending) if (!item.isEmpty())
            net.minecraft.world.level.block.Block.popResource(level,pos,item.copy());
        clearContent();
        pending.replaceAll(x -> ItemStack.EMPTY);
    }
    @Override public int getContainerSize() { return 27; }
    @Override public boolean isEmpty() { for (ItemStack s:stacks) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return stacks.get(slot); }
    @Override public ItemStack removeItem(int slot,int count) { ItemStack s=ContainerHelper.removeItem(stacks,slot,count); if (!s.isEmpty()) setChanged();return s; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(stacks,slot); }
    @Override public void setItem(int slot,ItemStack stack) {
        if (slot<0 || slot>=27 || (slot!=0 && slot!=1 && slot!=2 && !(automatic() && slot>=9 && slot<18))) return;
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
    @Override public int[] getSlotsForFace(Direction face) { return face==Direction.DOWN ? BOTTOM : face==Direction.UP ? TOP : SIDE; }
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face) { return canPlaceItem(slot,stack); }
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction face) { return face==Direction.DOWN && slot>=9 && slot<18; }
    @Override public Component getDisplayName() { return Component.translatable(automatic()?"block.greenhouse.automatic_greenhouse":"block.greenhouse.greenhouse"); }
    @Override public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        // Alpha GUI: vanilla chest inventory, first row: 3 inputs; second row: outputs (hopping).
        // Manual harvest is Shift+right click; a custom 'Raccogli' button will come in the next UI revision.
        return new GreenhouseMenu(windowId,inventory,this);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output,stacks);
        ContainerHelper.saveAllItems(output.child("pending"),pending);
        output.putInt("elapsed",elapsed);output.putInt("duration",duration);
        output.putInt("chargedCycles",chargedCycles);
        output.putString("crop",active.getSerializedName());
        output.putBoolean("mature",mature); output.putBoolean("newCycle",isReadyForNewCycle);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input,stacks);
        ContainerHelper.loadAllItems(input.childOrEmpty("pending"),pending);
        elapsed=input.getIntOr("elapsed",0);duration=input.getIntOr("duration",0);
        chargedCycles=input.getIntOr("chargedCycles",0);
        try { active=Crop.valueOf(input.getStringOr("crop","empty").toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException ex) { active=Crop.EMPTY; }
        mature=input.getBooleanOr("mature",false);
        isReadyForNewCycle=input.getBooleanOr("newCycle",true);
    }
}
