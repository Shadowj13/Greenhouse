package it.bonsaigreenhouse.jei;

import it.bonsaigreenhouse.BonsaiGreenhouse;
import it.bonsaigreenhouse.Crop;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * One greenhouse crop as shown by JEI. All values come from {@link Crop}, the
 * same data the greenhouse uses to grow and harvest, so the catalog cannot
 * drift from the game. The three poplar colors share one sapling and are
 * shown as a single recipe whose leaf bonus cycles through the colors.
 */
public record CultivationRecipe(Crop crop) {
    /** Crops listed in JEI: every cultivable crop, poplar once. */
    public static List<CultivationRecipe> all() {
        return Arrays.stream(Crop.values())
            .filter(Crop::isCultivable)
            .filter(c -> c != Crop.POPLAR_ORANGE && c != Crop.POPLAR_YELLOW)
            .map(CultivationRecipe::new)
            .toList();
    }
    public Identifier id() {
        String name = crop.isPoplar() ? "poplar" : crop.getSerializedName();
        return Identifier.fromNamespaceAndPath(BonsaiGreenhouse.ID, "cultivation/" + name);
    }
    public ItemStack plant() { return stack(crop.plantId, 1); }
    public List<ItemStack> soils() { return crop.soilIds().stream().map(id -> stack(id, 1)).toList(); }
    public ItemStack mainProduct() { return stack(crop.dropId, crop.minYield()); }
    /** Bonus items in roll order; for poplar the leaf entry lists all three colors. */
    public List<List<ItemStack>> bonusItems() {
        List<Crop.Bonus> bonuses = crop.bonuses();
        return java.util.stream.IntStream.range(0, bonuses.size()).mapToObj(i -> {
            if (!crop.isPoplar() || !bonuses.get(i).itemId().endsWith("_poplar_leaves"))
                return List.of(stack(bonuses.get(i).itemId(), 1));
            return List.of(Crop.POPLAR_RED, Crop.POPLAR_ORANGE, Crop.POPLAR_YELLOW).stream()
                .map(color -> stack(color.bonuses().get(i).itemId(), 1)).toList();
        }).toList();
    }
    public boolean isPoplarLeaf(int bonusIndex) {
        return crop.isPoplar() && crop.bonuses().get(bonusIndex).itemId().endsWith("_poplar_leaves");
    }
    static ItemStack stack(String vanillaId, int count) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(vanillaId));
        return new ItemStack(item, Math.max(1, count));
    }
}
