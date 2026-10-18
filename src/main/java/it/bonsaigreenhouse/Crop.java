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
    BROWN_MUSHROOM("brown_mushroom", "brown_mushroom", "mycelium", 900, 1, 25);

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
        for (Crop c : values()) if (c!=EMPTY && c.plantId.equals(name)) return c;
        return EMPTY;
    }
    public boolean accepts(ItemStack substrate) {
        if (this==EMPTY || substrate.isEmpty()) return false;
        String s=id(substrate);
        if (soilId.equals(s)) return true;
        if (soilId.equals("dirt")) return s.equals("grass_block") || s.equals("podzol") || s.equals("rooted_dirt") || s.equals("coarse_dirt");
        if (soilId.equals("sand")) return s.equals("red_sand");
        if (soilId.equals("mycelium")) return s.equals("podzol");
        return false;
    }
    private static Item item(String id) { return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(id)); }
    private static void add(List<ItemStack> list, String id, int count) {
        Item item = item(id);
        if (item != null && count > 0) list.add(new ItemStack(item, count));
    }
    public List<ItemStack> roll(net.minecraft.util.RandomSource rng) {
        List<ItemStack> products = new ArrayList<>();
        if (this==EMPTY) return products;
        int main = guaranteed;
        if (this==MELON) main += rng.nextInt(3); // 2..4 slices
        else if (extraChance>0 && rng.nextInt(100)<extraChance) main++;
        add(products, dropId, main);
        if (this==WHEAT && rng.nextBoolean()) add(products,"wheat_seeds",1);
        if (this==BEETROOT && rng.nextBoolean()) add(products,"beetroot_seeds",1);
        if (this==POTATOES && rng.nextInt(100)<2) add(products,"poisonous_potato",1);
        if (this==CRIMSON_FUNGUS || this==WARPED_FUNGUS) {
            if (rng.nextInt(100)<10) add(products,this==CRIMSON_FUNGUS?"nether_wart_block":"warped_wart_block",1);
            if (rng.nextInt(100)<5) add(products,plantId,1);
            if (rng.nextInt(100)<2) add(products,"shroomlight",1);
        } else if (isTree()) {
            if (rng.nextInt(100)<20) add(products,"stick",1);
            if (rng.nextInt(100)<10) add(products,leafId(),1);
            if (rng.nextInt(100)<5) add(products,plantId,1);
            if ((this==OAK || this==DARK_OAK) && rng.nextInt(100)<5) add(products,"apple",1);
            if (this==MANGROVE && rng.nextInt(100)<10) add(products,"mangrove_roots",1);
        }
        return products;
    }
    public boolean isTree() { return ordinal()>0 && ordinal()<=WARPED_FUNGUS.ordinal(); }
    public boolean isPoplar() { return this==POPLAR_RED || this==POPLAR_ORANGE || this==POPLAR_YELLOW; }
    private String leafId() {
        if (this==AZALEA) return "azalea_leaves";
        if (this==FLOWERING_AZALEA) return "flowering_azalea_leaves";
        if (this==CRIMSON_FUNGUS || this==WARPED_FUNGUS) return "shroomlight";
        if (name().startsWith("POPLAR")) return getSerializedName().substring("poplar_".length())+"_poplar_leaves";
        return getSerializedName()+"_leaves";
    }
}
