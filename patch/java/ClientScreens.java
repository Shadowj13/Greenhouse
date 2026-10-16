package it.bonsaigreenhouse;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid=BonsaiGreenhouse.ID, value=Dist.CLIENT)
public final class ClientScreens {
    private ClientScreens() {}
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(BonsaiGreenhouse.NORMAL_MENU.get(),GreenhouseScreen::new);
        event.register(BonsaiGreenhouse.AUTO_MENU.get(),GreenhouseScreen::new);
    }
}
