package com.iso2t.easyconfig.api;

import com.iso2t.easyconfig.api.files.AbstractFileType;
import com.iso2t.easyconfig.api.files.FileTypes;
import com.iso2t.easyconfig.api.manager.ConfigManager;
import com.iso2t.easyconfig.platform.Services;

public class ConfigBuilder {

	/**
	 * Creates a new ConfigManager for the given class.
	 * This will automatically resolve the config file path based on the platform.
	 * <br>
	 * While it is not required to do it this way, it is recommended to remove boilerplate code.
	 * <br>
	 * Defaults to JSON5 format.
	 *
	 * @param clazz The class to create a ConfigManager for.
	 * @param modid The modid to use for the config file.
	 * @return A built config instance.
	 * @param <T> The type of the class.
	 */
	public static <T> T build (Class<T> clazz, String modid) {
		return build(clazz, modid, FileTypes.JSON5);
	}

	public static <T> T build (Class<T> clazz, String modid, FileTypes fileType) {
		ConfigManager<T> manager = new ConfigManager<>(clazz, Services.PLATFORM.getConfigDir().resolve(modid + "." + fileType.extension()), fileType);
		return manager.loadAndSave();
	}

	public static <T> T build (Class<T> clazz, String modid, Class<? extends AbstractFileType> fileType) {
		AbstractFileType type = instantiateFileType(fileType);
		ConfigManager<T> manager = new ConfigManager<>(clazz, Services.PLATFORM.getConfigDir().resolve(modid + "." + type.extension()), type);
		return manager.loadAndSave();
	}

	private static AbstractFileType instantiateFileType (Class<? extends AbstractFileType> cls) {
		try {
			return cls.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to instantiate file type " + cls.getName(), e);
		}
	}

}
