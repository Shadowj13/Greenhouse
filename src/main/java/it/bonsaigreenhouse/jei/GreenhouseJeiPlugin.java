package it.bonsaigreenhouse.jei;

import it.bonsaigreenhouse.BonsaiGreenhouse;
import it.bonsaigreenhouse.GreenhouseScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

/**
 * Optional JEI integration. JEI discovers this class through the annotation;
 * without JEI installed it is never loaded, so the mod has no hard dependency.
 */
@JeiPlugin
public final class GreenhouseJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(BonsaiGreenhouse.ID, "jei_plugin");

    @Override public Identifier getPluginUid() { return UID; }

    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CultivationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CultivationCategory.TYPE, CultivationRecipe.all());
    }

    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(CultivationCategory.TYPE,
            BonsaiGreenhouse.NORMAL_ITEM.get(), BonsaiGreenhouse.HOPPING_ITEM.get());
    }

    @Override public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Clicking the growth bar opens the cultivation catalog.
        registration.addRecipeClickArea(GreenhouseScreen.class, 13, 74, 150, 9, CultivationCategory.TYPE);
    }
}
