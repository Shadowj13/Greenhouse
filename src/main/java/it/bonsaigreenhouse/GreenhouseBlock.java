package it.bonsaigreenhouse;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.jetbrains.annotations.Nullable;

public class GreenhouseBlock extends BaseEntityBlock {
    public static final EnumProperty<Crop> CROP=EnumProperty.create("crop",Crop.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    // The twelve selection edges lie on the opaque base, corner posts and flat
    // roof rim. Keep these coordinates in sync with the model regression test.
    // A bounding box around decorative base trim leaves a wire cage in the glass.
    private static final VoxelShape SHAPE=Block.box(1.0,0.0,1.0,15.0,14.85,15.0);
    private static final VoxelShape COLLISION=Block.box(0.2,0.0,0.2,15.8,14.85,15.8);
    public final boolean automatic;
    public GreenhouseBlock(boolean automatic,Properties properties) {
        super(properties);
        this.automatic=automatic;
        registerDefaultState(stateDefinition.any().setValue(CROP,Crop.EMPTY).setValue(STAGE,0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
        builder.add(CROP,STAGE);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new GreenhouseBlockEntity(pos,state); }
    // Redstone: any change around the block makes the greenhouse re-read its
    // power on the next server tick; a powered greenhouse pauses growth.
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block neighbor,@Nullable Orientation orientation,boolean movedByPiston) {
        super.neighborChanged(state,level,pos,neighbor,orientation,movedByPiston);
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof GreenhouseBlockEntity greenhouse) greenhouse.markPowerDirty();
    }
    @Override public boolean onDestroyedByPlayer(BlockState state,Level level,BlockPos pos,Player player,
                                                 ItemStack tool,boolean willHarvest,FluidState fluid) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof GreenhouseBlockEntity greenhouse)) {
            return super.onDestroyedByPlayer(state,level,pos,player,tool,willHarvest,fluid);
        }
        boolean creativeDrop=player.preventsBlockDrops() && greenhouse.hasPackedContents();
        // Survival loot reads the removed block entity. Creative supplies its
        // packed item only after a successful removal.
        if (willHarvest || creativeDrop) greenhouse.preserveContentsForDrop();
        boolean removed;
        try {
            removed=super.onDestroyedByPlayer(state,level,pos,player,tool,willHarvest,fluid);
        } finally {
            greenhouse.clearPackedDropMark();
        }
        if (removed && creativeDrop) {
            ItemStack packed=new ItemStack(this);
            packed.applyComponents(greenhouse.collectComponents());
            ItemEntity drop=new ItemEntity(level,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,packed);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
        return removed;
    }

    static void onBlockDrops(BlockDropsEvent event) {
        // Level.destroyBlock generates drops while the block entity is still
        // in the world. Ordinary loot previews never post this event.
        if (event.getBlockEntity() instanceof GreenhouseBlockEntity greenhouse
                && event.getLevel().getBlockEntity(event.getPos())==greenhouse
                && event.getDrops().stream().anyMatch(drop -> greenhouse.isPackedDrop(drop.getItem()))) {
            greenhouse.preserveContentsForDrop();
        }
    }

    @Override protected void onExplosionHit(BlockState state,ServerLevel level,BlockPos pos,Explosion explosion,
                                            BiConsumer<ItemStack,BlockPos> dropConsumer) {
        if (!(level.getBlockEntity(pos) instanceof GreenhouseBlockEntity greenhouse)) {
            super.onExplosionHit(state,level,pos,explosion,dropConsumer);
            return;
        }
        try {
            super.onExplosionHit(state,level,pos,explosion,(stack,dropPos) -> {
                dropConsumer.accept(stack,dropPos);
                if (greenhouse.isPackedDrop(stack)) greenhouse.preserveContentsForDrop();
            });
        } finally {
            greenhouse.clearPackedDropMark();
        }
    }
    // Comparator: growth progress (normal) or output fill level (automatic).
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state,Level level,BlockPos pos,Direction direction) {
        return level.getBlockEntity(pos) instanceof GreenhouseBlockEntity greenhouse ? greenhouse.comparatorSignal() : 0;
    }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type,BonsaiGreenhouse.GREENHOUSE_ENTITY.get(),GreenhouseBlockEntity::serverTick);
    }
    // Sneak-use on an automatic greenhouse gives access to its underlying chest,
    // barrel, double chest or other menu-providing storage block.
    private static void openStorageBelow(Level level,BlockPos pos,Player player) {
        BlockPos below=pos.below();
        MenuProvider storage=level.getBlockState(below).getMenuProvider(level,below);
        if(storage==null && level.getBlockEntity(below) instanceof MenuProvider provider) storage=provider;
        if(storage!=null) player.openMenu(storage);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof GreenhouseBlockEntity greenhouse) {
            if(player.isShiftKeyDown()) {
                if(automatic) openStorageBelow(level,pos,player);
                else greenhouse.collectBy(player);
            } else player.openMenu(greenhouse);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(player.isShiftKeyDown()) {
            if(!level.isClientSide() && level.getBlockEntity(pos) instanceof GreenhouseBlockEntity greenhouse) {
                if(automatic) openStorageBelow(level,pos,player);
                else greenhouse.collectBy(player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
