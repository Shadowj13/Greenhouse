package it.bonsaigreenhouse.jade;

import it.bonsaigreenhouse.Crop;
import it.bonsaigreenhouse.GreenhouseBlock;
import it.bonsaigreenhouse.GreenhouseBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade tooltip for both greenhouses: crop, state, progress with remaining
 * time and bone meal boost. The server sends the growth state; without Jade
 * on the server the tooltip still shows the crop from the block state.
 */
public enum GreenhouseJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override public Identifier getUid() { return GreenhouseJadeDataProvider.UID; }

    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag tag = accessor.getServerData().getCompound(GreenhouseJadeDataProvider.KEY).orElse(null);
        Crop crop = tag != null ? crop(tag.getStringOr("crop", "empty")) : accessor.getBlockState().getValue(GreenhouseBlock.CROP);
        if (crop == Crop.EMPTY) {
            tooltip.add(Component.translatable("gui.greenhouse.jade.idle"));
            return;
        }
        tooltip.add(Component.translatable("gui.greenhouse.jade.crop", plantName(crop)));
        if (tag == null) return;
        int elapsed = tag.getIntOr("elapsed", 0), duration = tag.getIntOr("duration", 0);
        if (tag.getBooleanOr("mature", false)) {
            // Still mature means waiting: automatic outputs full, or four harvests ready.
            tooltip.add(Component.translatable(tag.getBooleanOr("automatic", false) ? "gui.greenhouse.output_full" : "gui.greenhouse.full"));
        } else if (tag.getBooleanOr("powered", false)) {
            tooltip.add(Component.translatable("gui.greenhouse.paused"));
        } else if (duration > 0) {
            int percent = Math.min(100, 100 * elapsed / duration);
            tooltip.add(Component.translatable("gui.greenhouse.jade.growing", percent,
                GreenhouseBlockEntity.remainingSeconds(elapsed, duration)));
        }
        int ready = tag.getIntOr("ready", 0);
        if (!tag.getBooleanOr("automatic", false) && ready > 0)
            tooltip.add(Component.translatable("gui.greenhouse.jade.ready_count", ready, GreenhouseBlockEntity.NORMAL_HARVESTS));
        int boost = tag.getIntOr("boost", 0);
        if (boost > 0) tooltip.add(Component.translatable("gui.greenhouse.jade.boost", boost));
    }

    private static Crop crop(String name) {
        try { return Crop.valueOf(name.toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException ex) { return Crop.EMPTY; }
    }

    private static Component plantName(Crop crop) {
        var item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(crop.plantId));
        return new ItemStack(item).getHoverName();
    }
}
