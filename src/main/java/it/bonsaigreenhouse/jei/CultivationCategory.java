package it.bonsaigreenhouse.jei;

import it.bonsaigreenhouse.BonsaiGreenhouse;
import it.bonsaigreenhouse.Crop;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * JEI page for greenhouse cultivation: plant and soil (both kept), the main
 * harvest and every independent bonus with its chance, and the cycle time.
 */
public final class CultivationCategory extends AbstractRecipeCategory<CultivationRecipe> {
    public static final IRecipeType<CultivationRecipe> TYPE =
        IRecipeType.create(BonsaiGreenhouse.ID, "cultivation", CultivationRecipe.class);
    private static final int WIDTH = 144, HEIGHT = 56;

    public CultivationCategory(IGuiHelper guiHelper) {
        super(TYPE, Component.translatable("gui.greenhouse.jei.category"),
            guiHelper.createDrawableItemLike(BonsaiGreenhouse.NORMAL_ITEM.get()), WIDTH, HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CultivationRecipe recipe, IFocusGroup focuses) {
        Crop crop = recipe.crop();
        builder.addInputSlot(1, 1)
            .setStandardSlotBackground()
            .add(recipe.plant())
            .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("gui.greenhouse.jei.not_consumed")));
        builder.addInputSlot(1, 21)
            .setStandardSlotBackground()
            .addItemStacks(recipe.soils())
            .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("gui.greenhouse.jei.not_consumed")));
        builder.addOutputSlot(56, 11)
            .setOutputSlotBackground()
            .add(recipe.mainProduct())
            .addRichTooltipCallback((view, tooltip) -> tooltip.add(yieldText(crop)));
        List<Crop.Bonus> bonuses = crop.bonuses();
        List<List<net.minecraft.world.item.ItemStack>> items = recipe.bonusItems();
        for (int i = 0; i < bonuses.size(); i++) {
            int chance = bonuses.get(i).chancePercent();
            boolean poplarLeaf = recipe.isPoplarLeaf(i);
            builder.addOutputSlot(88 + (i % 3) * 18, 1 + (i / 3) * 20)
                .setStandardSlotBackground()
                .addItemStacks(items.get(i))
                .addRichTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable("gui.greenhouse.jei.chance", chance));
                    if (poplarLeaf) tooltip.add(Component.translatable("gui.greenhouse.jei.poplar_color"));
                });
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, CultivationRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrowWidget().setPosition(25, 11);
        int ticks = recipe.crop().duration;
        Component time = Component.translatable("gui.greenhouse.jei.time", seconds(ticks), seconds(ticks / 2));
        builder.addText(time, WIDTH, 12)
            .setPosition(0, HEIGHT - 12)
            .setTextAlignment(HorizontalAlignment.CENTER)
            .setColor(0xFF404040);
    }

    @Override
    public Identifier getIdentifier(CultivationRecipe recipe) { return recipe.id(); }

    @Override
    public List<Identifier> getSourceRecipeIds(CultivationRecipe recipe) { return List.of(); }

    private static Component yieldText(Crop crop) {
        int min = crop.minYield(), max = crop.maxYield();
        if (min == max) return Component.translatable("gui.greenhouse.jei.yield", min);
        if (crop == Crop.MELON) return Component.translatable("gui.greenhouse.jei.yield_uniform", min, max);
        return Component.translatable("gui.greenhouse.jei.yield_extra", min, max, max, crop.extraChance);
    }

    /** Ticks at 20 TPS as seconds, with the decimal separator of the current language. */
    private static String seconds(int ticks) {
        if (ticks % 20 == 0) return Integer.toString(ticks / 20);
        String separator = Component.translatable("gui.greenhouse.jei.decimal").getString();
        return (ticks / 20) + separator + ((ticks % 20) * 10 / 20);
    }
}
