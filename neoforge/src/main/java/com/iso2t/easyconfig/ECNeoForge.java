package com.iso2t.easyconfig;

import com.iso2t.easyconfig.client.NeoForgeMenuBranding;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class ECNeoForge {

    public ECNeoForge (IEventBus eventBus, ModContainer modContainer) {
        EasyConfig.init();
		if (FMLEnvironment.getDist() == Dist.CLIENT) NeoForgeMenuBranding.register();
    }
}