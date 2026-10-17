package it.bonsaigreenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GreenhouseBlock extends BaseEntityBlock {
    public static final EnumProperty<Crop> CROP=EnumProperty.create("crop",Crop.class);
    public static final IntegerProperty STAGE=IntegerProperty.create("stage",0,3);
    // One clean selection cuboid replaces the old overlapping 13-part wireframe.
    // Detailed timber/glass geometry remains visual, not part of the targeting outline.
    private static final VoxelShape SHAPE=Block.box(0.45,0.0,0.45,15.55,15.55,15.55);
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
        return SHAPE;
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new GreenhouseBlockEntity(pos,state); }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type,BonsaiGreenhouse.GREENHOUSE_ENTITY.get(),GreenhouseBlockEntity::serverTick);
    }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player) {
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof GreenhouseBlockEntity greenhouse)
            greenhouse.dropContentsForBreak(level,pos);
        return super.playerWillDestroy(level,pos,state,player);
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
