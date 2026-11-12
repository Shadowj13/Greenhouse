package it.bonsaigreenhouse;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(BonsaiGreenhouse.ID)
public final class BonsaiGreenhouse {
    public static final String ID="greenhouse";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU,ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ID);
    // Shaped recipe that keeps the contents of the greenhouse ingredient (upgrade to automatic).
    public static final DeferredHolder<net.minecraft.world.item.crafting.RecipeSerializer<?>,net.minecraft.world.item.crafting.RecipeSerializer<KeepContentsShapedRecipe>> KEEP_CONTENTS_SHAPED =
        RECIPE_SERIALIZERS.register("keep_contents_shaped", () -> KeepContentsShapedRecipe.SERIALIZER);

    public static final DeferredBlock<GreenhouseBlock> NORMAL = BLOCKS.registerBlock("greenhouse",
        p -> new GreenhouseBlock(false,p), p -> p.mapColor(MapColor.WOOD).strength(1.5F).noOcclusion());
    public static final DeferredBlock<GreenhouseBlock> HOPPING = BLOCKS.registerBlock("automatic_greenhouse",
        p -> new GreenhouseBlock(true,p), p -> p.mapColor(MapColor.WOOD).strength(2.5F).noOcclusion());
    public static final DeferredItem<BlockItem> NORMAL_ITEM = ITEMS.registerSimpleBlockItem("greenhouse", NORMAL);
    public static final DeferredItem<BlockItem> HOPPING_ITEM = ITEMS.registerSimpleBlockItem("automatic_greenhouse", HOPPING);

    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<GreenhouseBlockEntity>> GREENHOUSE_ENTITY =
        BLOCK_ENTITIES.register("greenhouse", () -> new BlockEntityType<>(GreenhouseBlockEntity::new,NORMAL.get(),HOPPING.get()));

    public static final DeferredHolder<MenuType<?>,MenuType<GreenhouseMenu>> NORMAL_MENU =
        MENUS.register("greenhouse", () -> new MenuType<>((id,inv) -> new GreenhouseMenu(id,inv,false),FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>,MenuType<GreenhouseMenu>> AUTO_MENU =
        MENUS.register("automatic_greenhouse", () -> new MenuType<>((id,inv) -> new GreenhouseMenu(id,inv,true),FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TAB = TABS.register("greenhouse", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.greenhouse"))
        .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
        .icon(() -> NORMAL_ITEM.get().getDefaultInstance())
        .displayItems((parameters, output) -> { output.accept(NORMAL_ITEM.get()); output.accept(HOPPING_ITEM.get()); })
        .build());

    public BonsaiGreenhouse(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        TABS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(RegisterCapabilitiesEvent.class, BonsaiGreenhouse::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,BlockDropsEvent.class,GreenhouseBlock::onBlockDrops);
    }

    /**
     * Expose both greenhouses to pipes and storage from other mods. NeoForge
     * wraps only vanilla block entities automatically; the wrapper applies the
     * same face rules as hoppers: soil, plant and bone meal from above, bone
     * meal from the sides, products extracted from below.
     */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, GREENHOUSE_ENTITY.get(),
            (greenhouse, side) -> side==null ? null : new WorldlyContainerWrapper(greenhouse, side));
    }
}
