package com.iso2t.easyconfig;

import com.iso2t.easyconfig.api.ConfigBuildOptions;
import com.iso2t.easyconfig.api.ConfigBuilder;
import com.iso2t.easyconfig.api.ConfigPlatform;
import com.iso2t.easyconfig.api.files.FileTypes;
import com.iso2t.easyconfig.config.ModConfig;
import com.iso2t.easyconfig.platform.Services;

public class EasyConfig {

	public static ModConfig CONFIG = null;

	public static void init () {
		if (Services.PLATFORM.isDevelopmentEnvironment()) Constants.LOG.warn("{} is in development mode! Is this intentional?", Constants.MOD_NAME);

		ConfigPlatform.configure(Services.PLATFORM.getConfigDir(), Services.PLATFORM::registerConfigScreen);
		CONFIG = ConfigBuilder.build(ModConfig.class, Constants.MOD_ID, FileTypes.TOML, ConfigBuildOptions.defaults().screenTitle(Constants.MOD_NAME));
	}

	public static ModConfig getConfig () {
		return CONFIG;
	}

}
