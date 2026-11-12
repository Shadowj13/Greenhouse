package it.bonsaigreenhouse;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * A normal shaped crafting recipe whose result keeps the contents of the
 * greenhouse used as an ingredient. Upgrading a packed greenhouse to an
 * automatic one therefore keeps its soil, plant, bone meal and ready harvest.
 * It works in the crafting table, the crafter and other mods' autocrafters,
 * because the copy happens in {@link #assemble}. The JSON format is the same
 * as minecraft:crafting_shaped.
 */
public final class KeepContentsShapedRecipe extends ShapedRecipe {
    public static final MapCodec<KeepContentsShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Recipe.CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
        CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
        ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.resultTemplate)
    ).apply(i, KeepContentsShapedRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KeepContentsShapedRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, r -> r.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC, r -> r.bookInfo,
        ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
        ItemStackTemplate.STREAM_CODEC, r -> r.resultTemplate,
        KeepContentsShapedRecipe::new);
    public static final RecipeSerializer<KeepContentsShapedRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate resultTemplate;

    public KeepContentsShapedRecipe(Recipe.CommonInfo common, CraftingRecipe.CraftingBookInfo book,
                                    ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(common, book, pattern, result);
        this.resultTemplate = result;
    }

    @Override public ItemStack assemble(CraftingInput input) {
        ItemStack result = super.assemble(input);
        for (ItemStack ingredient : input.items()) {
            if (ingredient.is(BonsaiGreenhouse.NORMAL_ITEM.get())) {
                result.applyComponents(ingredient.getComponentsPatch());
                break;
            }
        }
        return result;
    }

    // ShapedRecipe fixes the serializer type to its own class; the registered
    // serializer reads and writes exactly this subclass.
    @SuppressWarnings("unchecked")
    @Override public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }
}
