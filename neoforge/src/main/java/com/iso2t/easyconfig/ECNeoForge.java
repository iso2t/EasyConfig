package com.iso2t.easyconfig;

import com.iso2t.easyconfig.client.NeoForgeConfigScreenOpener;
import com.iso2t.easyconfig.client.NeoForgeMenuBranding;
import com.iso2t.easyconfig.platform.NeoForgePlatformHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class ECNeoForge {

    public ECNeoForge (IEventBus eventBus, ModContainer modContainer) {
        EasyConfig.init();
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			NeoForgeMenuBranding.register();
			NeoForgeConfigScreenOpener.register(eventBus);
			eventBus.addListener(ECNeoForge::registerKnownConfigScreens);
		}
    }

	private static void registerKnownConfigScreens (FMLClientSetupEvent event) {
		NeoForgePlatformHelper.registerKnownConfigScreens();
	}
}
