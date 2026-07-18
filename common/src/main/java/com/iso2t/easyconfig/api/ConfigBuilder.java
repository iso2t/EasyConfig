package com.iso2t.easyconfig.api;

import com.iso2t.easyconfig.api.manager.ConfigManager;
import com.iso2t.easyconfig.platform.Services;

public class ConfigBuilder {

	/**
	 * Creates a new ConfigManager for the given class.
	 * This will automatically resolve the config file path based on the platform.
	 * <br>
	 * While it is not required to do it this way, it is recommended to remove boilerplate code.
	 *
	 * @param clazz The class to create a ConfigManager for.
	 * @param modid The modid to use for the config file.
	 * @return A new ConfigManager instance.
	 * @param <T> The type of the class.
	 */
	public static <T> T builder (Class<T> clazz, String modid) {
		ConfigManager<T> manager = new ConfigManager<>(clazz, Services.PLATFORM.getConfigDir().resolve(modid + ".json5"));
		return manager.loadAndSave();
	}

}
