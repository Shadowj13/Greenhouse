package it.bonsaigreenhouse;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class GreenhouseMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final boolean automatic;
    private final GreenhouseBlockEntity blockEntity;
    private final int machineSlots;

    public GreenhouseMenu(int id, Inventory inventory, boolean automatic) {
        this(id, inventory, new SimpleContainer(27), new SimpleContainerData(4), automatic, null);
    }
    public GreenhouseMenu(int id, Inventory inventory, GreenhouseBlockEntity be) {
        this(id, inventory, be, be.dataAccess(), be.isAutomatic(), be);
    }
    private GreenhouseMenu(int id, Inventory inventory, Container storage,
                           ContainerData data, boolean automatic, GreenhouseBlockEntity be) {
        super(automatic ? BonsaiGreenhouse.AUTO_MENU.get() : BonsaiGreenhouse.NORMAL_MENU.get(), id);
        this.container = storage;
        this.data = data;
        this.automatic = automatic;
        this.blockEntity = be;
        this.machineSlots = automatic ? 12 : 3;
        checkContainerSize(storage, 27);
        checkContainerDataCount(data, 4);
        storage.startOpen(inventory.player);

        addSlot(new Slot(storage,0,24,39) {
            @Override public boolean mayPlace(ItemStack stack) {
                for (Crop crop: Crop.values()) if (crop.accepts(stack)) return true;
                return false;
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(storage,1,80,39) {
            @Override public boolean mayPlace(ItemStack stack) { return Crop.of(stack) != Crop.EMPTY; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(storage,2,136,39) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.BONE_MEAL); }
        });
        if (automatic) for (int i=0;i<9;i++) {
            addSlot(new Slot(storage,9+i,8+i*18,107) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }
        for (int row=0;row<3;row++) for (int col=0;col<9;col++) {
            addSlot(new Slot(inventory,9+row*9+col,8+col*18,139+row*18));
        }
        for (int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,195));
        addDataSlots(data);
    }
    public boolean isAutomatic() { return automatic; }
    public int elapsed() { return data.get(0); }
    public int duration() { return data.get(1); }
    public boolean mature() { return data.get(2)!=0; }
    public int bonusCycles() { return data.get(3); }
    public Crop crop() { return Crop.of(container.getItem(1)); }
    public boolean validCrop() {
        Crop crop=crop(); return crop!=Crop.EMPTY && crop.accepts(container.getItem(0));
    }
    @Override public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId==0 && !automatic && blockEntity!=null && stillValid(player)) {
            blockEntity.collectBy(player);
            return true;
        }
        return false;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack=slot.getItem();
        ItemStack initial=stack.copy();
        if (index<machineSlots) {
            if (!moveItemStackTo(stack,machineSlots,slots.size(),true)) return ItemStack.EMPTY;
        } else {
            int target=-1;
            if (stack.is(Items.BONE_MEAL)) target=2;
            else if (Crop.of(stack)!=Crop.EMPTY) target=1;
            else for (Crop crop:Crop.values()) if (crop.accepts(stack)) {target=0; break;}
            if (target<0 || !moveItemStackTo(stack,target,target+1,false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return initial;
    }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
