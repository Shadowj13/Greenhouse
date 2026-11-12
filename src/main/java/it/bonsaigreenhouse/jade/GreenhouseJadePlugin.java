package it.bonsaigreenhouse.jade;

import it.bonsaigreenhouse.GreenhouseBlock;
import it.bonsaigreenhouse.GreenhouseBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Optional Jade integration. Jade discovers this class through the annotation;
 * without Jade installed it is never loaded.
 */
@WailaPlugin
public final class GreenhouseJadePlugin implements IWailaPlugin {
    @Override public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(GreenhouseJadeDataProvider.INSTANCE, GreenhouseBlockEntity.class);
    }

    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(GreenhouseJadeProvider.INSTANCE, GreenhouseBlock.class);
    }
}
