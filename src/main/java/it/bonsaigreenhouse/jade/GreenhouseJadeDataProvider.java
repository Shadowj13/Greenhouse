package it.bonsaigreenhouse.jade;

import it.bonsaigreenhouse.BonsaiGreenhouse;
import it.bonsaigreenhouse.GreenhouseBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum GreenhouseJadeDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier UID = Identifier.fromNamespaceAndPath(BonsaiGreenhouse.ID, "greenhouse_info");
    static final String KEY = "greenhouse";

    @Override public Identifier getUid() { return UID; }

    @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof GreenhouseBlockEntity greenhouse)) return;
        var values = greenhouse.dataAccess();
        CompoundTag tag = new CompoundTag();
        tag.putString("crop", greenhouse.activeCrop().getSerializedName());
        tag.putInt("elapsed", values.get(0));
        tag.putInt("duration", values.get(1));
        tag.putBoolean("mature", values.get(2) != 0);
        tag.putInt("boost", values.get(3));
        tag.putBoolean("powered", values.get(4) != 0);
        tag.putBoolean("automatic", greenhouse.isAutomatic());
        tag.putInt("ready", greenhouse.readyHarvests());
        data.put(KEY, tag);
    }
}
