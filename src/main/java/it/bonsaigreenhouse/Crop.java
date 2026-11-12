package it.bonsaigreenhouse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** All available crops; timings and rolls match the design agreed for alpha. */
public enum Crop implements StringRepresentable {
    EMPTY("", "", "", 0, 0, 0),
    OAK("oak_sapling", "oak_log", "dirt", 900, 2, 0),
    SPRUCE("spruce_sapling", "spruce_log", "dirt", 900, 2, 0),
    BIRCH("birch_sapling", "birch_log", "dirt", 900, 2, 0),
    JUNGLE("jungle_sapling", "jungle_log", "dirt", 900, 2, 0),
    ACACIA("acacia_sapling", "acacia_log", "dirt", 900, 2, 0),
    DARK_OAK("dark_oak_sapling", "dark_oak_log", "dirt", 900, 2, 0),
    CHERRY("cherry_sapling", "cherry_log", "dirt", 900, 2, 0),
    MANGROVE("mangrove_propagule", "mangrove_log", "dirt", 900, 2, 0),
    PALE_OAK("pale_oak_sapling", "pale_oak_log", "dirt", 900, 2, 0),
    AZALEA("azalea", "oak_log", "dirt", 900, 2, 0),
    FLOWERING_AZALEA("flowering_azalea", "oak_log", "dirt", 900, 2, 0),
    POPLAR_RED("poplar_sapling", "poplar_log", "dirt", 900, 2, 0),
    POPLAR_ORANGE("poplar_sapling", "poplar_log", "dirt", 900, 2, 0),
    POPLAR_YELLOW("poplar_sapling", "poplar_log", "dirt", 900, 2, 0),
    CRIMSON_FUNGUS("crimson_fungus", "crimson_stem", "crimson_nylium", 900, 2, 0),
    WARPED_FUNGUS("warped_fungus", "warped_stem", "warped_nylium", 900, 2, 0),
    WHEAT("wheat_seeds", "wheat", "dirt", 600, 2, 0),
    CARROTS("carrot", "carrot", "dirt", 600, 1, 50),
    POTATOES("potato", "potato", "dirt", 600, 1, 50),
    BEETROOT("beetroot_seeds", "beetroot", "dirt", 600, 2, 0),
    NETHER_WART("nether_wart", "nether_wart", "soul_sand", 900, 2, 0),
    COCOA("cocoa_beans", "cocoa_beans", "jungle_log", 900, 1, 50),
    CACTUS("cactus", "cactus", "sand", 600, 1, 0),
    SUGAR_CANE("sugar_cane", "sugar_cane", "dirt", 600, 1, 0),
    BAMBOO("bamboo", "bamboo", "dirt", 900, 1, 50),
    PUMPKIN("pumpkin_seeds", "pumpkin", "dirt", 900, 1, 0),
    MELON("melon_seeds", "melon_slice", "dirt", 900, 2, 100),
    SWEET_BERRIES("sweet_berries", "sweet_berries", "dirt", 900, 1, 50),
    RED_MUSHROOM("red_mushroom", "red_mushroom", "mycelium", 900, 1, 25),
    BROWN_MUSHROOM("brown_mushroom", "brown_mushroom", "mycelium", 900, 1, 25),
    // Append new crops at the end to keep the block state ordering stable.
    // Tree membership is explicit (see TREES), not derived from the position.
    KELP("kelp", "kelp", "water_bucket", 600, 1, 0),
    GLOW_BERRIES("glow_berries", "glow_berries", "dirt", 900, 1, 50),
    DANDELION("dandelion", "dandelion", "dirt", 600, 1, 0),
    POPPY("poppy", "poppy", "dirt", 600, 1, 0),
    BLUE_ORCHID("blue_orchid", "blue_orchid", "dirt", 600, 1, 0),
    ALLIUM("allium", "allium", "dirt", 600, 1, 0),
    AZURE_BLUET("azure_bluet", "azure_bluet", "dirt", 600, 1, 0),
    RED_TULIP("red_tulip", "red_tulip", "dirt", 600, 1, 0),
    ORANGE_TULIP("orange_tulip", "orange_tulip", "dirt", 600, 1, 0),
    WHITE_TULIP("white_tulip", "white_tulip", "dirt", 600, 1, 0),
    PINK_TULIP("pink_tulip", "pink_tulip", "dirt", 600, 1, 0),
    OXEYE_DAISY("oxeye_daisy", "oxeye_daisy", "dirt", 600, 1, 0),
    CORNFLOWER("cornflower", "cornflower", "dirt", 600, 1, 0),
    LILY_OF_THE_VALLEY("lily_of_the_valley", "lily_of_the_valley", "dirt", 600, 1, 0),
    SUNFLOWER("sunflower", "sunflower", "dirt", 600, 1, 0),
    LILAC("lilac", "lilac", "dirt", 600, 1, 0),
    PEONY("peony", "peony", "dirt", 600, 1, 0),
    ROSE_BUSH("rose_bush", "rose_bush", "dirt", 600, 1, 0),
    SEA_PICKLE("sea_pickle", "sea_pickle", "water_bucket", 600, 1, 0),
    // Keep the serialized state and ordinal for older worlds.
    MOSS("moss_block", "moss_block", "stone", 600, 1, 0),
    CHORUS("chorus_flower", "chorus_fruit", "end_stone", 900, 1, 50),
    VINE("vine", "vine", "dirt", 600, 1, 0),
    GLOW_LICHEN("glow_lichen", "glow_lichen", "stone", 600, 1, 0),
    TWISTING_VINES("twisting_vines", "twisting_vines", "warped_nylium", 600, 1, 0),
    WEEPING_VINES("weeping_vines", "weeping_vines", "crimson_nylium", 600, 1, 0),
    BIG_DRIPLEAF("big_dripleaf", "big_dripleaf", "dirt", 600, 1, 0),
    SHELF_MUSHROOM("shelf_mushroom", "shelf_mushroom", "mycelium", 900, 1, 25);

    public final String plantId, dropId, soilId;
    public final int duration, guaranteed, extraChance;
    Crop(String plantId, String dropId, String soilId, int duration, int guaranteed, int extraChance) {
        this.plantId=plantId; this.dropId=dropId; this.soilId=soilId;
        this.duration=duration; this.guaranteed=guaranteed; this.extraChance=extraChance;
    }
    @Override public String getSerializedName() { return name().toLowerCase(Locale.ROOT); }
    private static String id(ItemStack stack) {
        Identifier name = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return name != null && name.getNamespace().equals("minecraft") ? name.getPath() : "";
    }
    public static Crop of(ItemStack stack) {
        if (stack.isEmpty()) return EMPTY;
        String name = id(stack);
        for (Crop c : values()) if (c.isCultivable() && c.plantId.equals(name)) return c;
        return EMPTY;
    }
    public boolean isCultivable() { return this!=EMPTY && this!=MOSS; }
    /**
     * Every vanilla substrate accepted by this crop. This is the single source
     * for both the cultivation rule and the JEI catalog.
     */
    public List<String> soilIds() {
        if (!isCultivable()) return List.of();
        return switch (soilId) {
            case "dirt" -> List.of("dirt", "grass_block", "podzol", "rooted_dirt", "coarse_dirt");
            case "sand" -> List.of("sand", "red_sand");
            case "mycelium" -> List.of("mycelium", "podzol");
            default -> List.of(soilId);
        };
    }
    public boolean accepts(ItemStack substrate) {
        if (!isCultivable() || substrate.isEmpty()) return false;
        return soilIds().contains(id(substrate));
    }
    /** Smallest and largest main yield of one harvest. */
    public int minYield() { return guaranteed; }
    public int maxYield() { return this==MELON ? guaranteed+2 : extraChance>0 ? guaranteed+1 : guaranteed; }
    /** An independent extra item: each one is rolled separately at every harvest. */
    public record Bonus(String itemId, int chancePercent) {}
    /**
     * Extra items of one harvest, in roll order. This table drives both the
     * actual harvest ({@link #roll}) and the JEI catalog, so they cannot diverge.
     */
    public List<Bonus> bonuses() {
        List<Bonus> list = new ArrayList<>();
        if (this==WHEAT) list.add(new Bonus("wheat_seeds", 50));
        if (this==BEETROOT) list.add(new Bonus("beetroot_seeds", 50));
        if (this==POTATOES) list.add(new Bonus("poisonous_potato", 2));
        if (this==CRIMSON_FUNGUS || this==WARPED_FUNGUS) {
            list.add(new Bonus(this==CRIMSON_FUNGUS ? "nether_wart_block" : "warped_wart_block", 10));
            list.add(new Bonus(plantId, 5));
            list.add(new Bonus("shroomlight", 2));
        } else if (isTree()) {
            list.add(new Bonus("stick", 20));
            list.add(new Bonus(leafId(), 10));
            list.add(new Bonus(plantId, 5));
            if (this==OAK || this==DARK_OAK) list.add(new Bonus("apple", 5));
            if (this==MANGROVE) list.add(new Bonus("mangrove_roots", 10));
        }
        return List.copyOf(list);
    }
    private static Item item(String id) { return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(id)); }
    private static void add(List<ItemStack> list, String id, int count) {
        Item item = item(id);
        if (item != null && count > 0) list.add(new ItemStack(item, count));
    }
    public List<ItemStack> roll(net.minecraft.util.RandomSource rng) {
        List<ItemStack> products = new ArrayList<>();
        if (!isCultivable()) return products;
        int main = guaranteed;
        if (this==MELON) main += rng.nextInt(3); // 2..4 slices
        else if (extraChance>0 && rng.nextInt(100)<extraChance) main++;
        add(products, dropId, main);
        for (Bonus bonus : bonuses()) if (rng.nextInt(100)<bonus.chancePercent()) add(products, bonus.itemId(), 1);
        return products;
    }
    /** Crops with tree-style bonuses (logs/stems, sticks, leaves, saplings). */
    private static final java.util.Set<Crop> TREES = java.util.Collections.unmodifiableSet(java.util.EnumSet.of(
        OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, CHERRY, MANGROVE, PALE_OAK,
        AZALEA, FLOWERING_AZALEA, POPLAR_RED, POPLAR_ORANGE, POPLAR_YELLOW,
        CRIMSON_FUNGUS, WARPED_FUNGUS));
    public boolean isTree() { return TREES.contains(this); }
    public boolean isPoplar() { return this==POPLAR_RED || this==POPLAR_ORANGE || this==POPLAR_YELLOW; }
    private String leafId() {
        if (this==AZALEA) return "azalea_leaves";
        if (this==FLOWERING_AZALEA) return "flowering_azalea_leaves";
        if (name().startsWith("POPLAR")) return getSerializedName().substring("poplar_".length())+"_poplar_leaves";
        return getSerializedName()+"_leaves";
    }
}
