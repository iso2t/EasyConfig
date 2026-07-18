package com.iso2t.easyconfig;

import com.iso2t.easyconfig.api.ConfigBuilder;
import com.iso2t.easyconfig.config.ModConfig;
import com.iso2t.easyconfig.platform.Services;

public class EasyConfig {

	public static ModConfig CONFIG = null;

    public static void init() {
		if (Services.PLATFORM.isDevelopmentEnvironment()) Constants.LOG.warn("{} is in development mode! Is this intentional?", Constants.MOD_NAME);

		CONFIG = ConfigBuilder.build(ModConfig.class, Constants.MOD_ID);
    }

	public static ModConfig getConfig() {
		return CONFIG;
	}

}
