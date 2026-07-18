package com.iso2t.easyconfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ECNeoForge {

    public ECNeoForge (IEventBus eventBus, ModContainer modContainer) {
        EasyConfig.init();
    }
}