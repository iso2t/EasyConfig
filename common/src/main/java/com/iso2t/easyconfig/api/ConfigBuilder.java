package com.iso2t.easyconfig.api;

import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.files.AbstractFileType;
import com.iso2t.easyconfig.api.files.FileTypes;
import com.iso2t.easyconfig.api.manager.ConfigManager;
import com.iso2t.easyconfig.api.registry.ConfigRegistry;
import com.iso2t.easyconfig.platform.Services;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class ConfigBuilder {

	/**
	 * Builds and initializes a configuration object for the given class and mod identifier.
	 * The configuration is automatically set to use the JSON5 file format.
	 *
	 * @param <T>   The type of the configuration class.
	 * @param clazz The {@code Class} object representing the configuration class to be created.
	 *              Must not be null.
	 * @param modid A unique identifier for the mod using the configuration.
	 *              This is used to resolve the configuration file path.
	 *              Must not be null or empty.
	 * @return An instance of the specified configuration class, initialized with data loaded
	 *         from the configuration file.
	 * @throws IllegalStateException If an error occurs during configuration file creation,
	 *                               loading, or if reflection-based initialization fails.
	 */
	public static <T> T build (Class<T> clazz, String modid) {
		return build(clazz, modid, FileTypes.JSON5, ConfigBuildOptions.defaults());
	}

	public static <T> T build (Class<T> clazz, String modid, ConfigBuildOptions options) {
		return build(clazz, modid, FileTypes.JSON5, options);
	}

	/**
	 * Builds and initializes a configuration object for the given class, mod identifier, and file type.
	 * The configuration is automatically loaded from and saved to a file determined by the class, mod identifier, and file type.
	 *
	 * @param <T>     The type of the configuration class.
	 * @param clazz   The {@code Class} object representing the configuration class to be created.
	 *                Must not be null.
	 * @param modid   A unique identifier for the mod using the configuration.
	 *                This is used to resolve the configuration file path.
	 *                Must not be null or empty.
	 * @param fileType The file type used to determine the file format and extension.
	 *                 Must not be null.
	 * @return An instance of the specified configuration class, initialized with data loaded
	 *         from the configuration file.
	 * @throws IllegalStateException If an error occurs during configuration file creation, loading,
	 *                               or if reflection-based initialization fails.
	 */
	public static <T> T build (Class<T> clazz, String modid, FileTypes fileType) {
		return build(clazz, modid, fileType, ConfigBuildOptions.defaults());
	}

	public static <T> T build (Class<T> clazz, String modid, FileTypes fileType, ConfigBuildOptions options) {
		ConfigManager<T> manager = new ConfigManager<>(clazz, resolveConfigPath(clazz, modid, fileType.extension()), fileType);
		T config = manager.loadAndSave();
		registerIfNeeded(clazz, modid, manager, config, options);
		return config;
	}

	/**
	 * Builds and initializes a configuration object for the given class, mod identifier,
	 * and file type. The configuration is loaded from and saved to a file determined
	 * by the provided class, mod identifier, and file type.
	 *
	 * @param <T>      The type of the configuration class.
	 * @param clazz    The {@code Class} object representing the configuration
	 *                 class to be created. Must not be null.
	 * @param modid    A unique identifier for the mod using the configuration.
	 *                 This is used to resolve the configuration file path.
	 *                 Must not be null or empty.
	 * @param fileType The {@code Class} object representing a specific subclass of
	 *                 {@code AbstractFileType}, used to determine the file format
	 *                 and extension. Must not be null and should have a public
	 *                 no-argument constructor.
	 * @return An instance of the specified configuration class, initialized with
	 *         data loaded from the configuration file.
	 * @throws IllegalStateException If an error occurs during any of the following:
	 *                               file path resolution, file creation, file loading,
	 *                               or reflection-based initialization.
	 */
	public static <T> T build (Class<T> clazz, String modid, Class<? extends AbstractFileType> fileType) {
		return build(clazz, modid, fileType, ConfigBuildOptions.defaults());
	}

	public static <T> T build (Class<T> clazz, String modid, Class<? extends AbstractFileType> fileType, ConfigBuildOptions options) {
		AbstractFileType type = instantiateFileType(fileType);
		ConfigManager<T> manager = new ConfigManager<>(clazz, resolveConfigPath(clazz, modid, type.extension()), type);
		T config = manager.loadAndSave();
		registerIfNeeded(clazz, modid, manager, config, options);
		return config;
	}

	/**
	 * Resolves the file path for a configuration file based on the given class,
	 * fallback name, and file extension. The resolved path is generated by combining
	 * the platform's configuration directory and the constructed configuration file name.
	 *
	 * @param clazz The class for which the configuration file path is being resolved. Must not be null.
	 * @param fallbackName The fallback name to use for the configuration file if no {@link Config}
	 *                     annotation is present or its name value is blank. Must not be null or empty.
	 * @param extension The file extension to append to the resolved configuration file name. Must not be null or empty.
	 * @return The resolved {@code Path} representing the complete configuration file location.
	 */
	private static Path resolveConfigPath (Class<?> clazz, String fallbackName, String extension) {
		return Services.PLATFORM.getConfigDir().resolve(configFileName(clazz, fallbackName, extension));
	}

	/**
	 * Constructs a configuration file name for a given class based on its {@link Config} annotation,
	 * a fallback name, and a specified file extension. If the {@link Config} annotation is present
	 * and its name value is not blank, the annotation's name value is used; otherwise, the fallback
	 * name is used. A side-specific suffix is appended based on the {@link Config} annotation's
	 * {@code side()} value, and the resulting name is combined with the specified file extension.
	 *
	 * @param clazz The class for which the configuration file name is being constructed. Must not be null.
	 * @param fallbackName The fallback name to use for the configuration file if no {@link Config}
	 *                     annotation is present or its name value is blank. Must not be null or empty.
	 * @param extension The file extension to append to the constructed configuration file name. Must not be null or empty.
	 * @return The fully constructed configuration file name, including the side-specific suffix and file extension.
	 */
	private static String configFileName (Class<?> clazz, String fallbackName, String extension) {
		Config config = clazz.getAnnotation(Config.class);
		String name = config != null && !config.name().isBlank() ? config.name() : fallbackName;
		return name + sideSuffix(config) + "." + extension;
	}

	/**
	 * Appends a side-specific suffix to a configuration name based on the given Config object's side.
	 * Returns an empty string if the Config object is null or if the side is COMMON.
	 *
	 * @param config The Config object used to determine the side-specific suffix. Can be null.
	 * @return A string representing the side-specific suffix: "-client" for CLIENT, "-server" for SERVER,
	 *         or an empty string for COMMON or if the provided Config object is null.
	 */
	private static String sideSuffix (Config config) {
		if (config == null) return "";

		return switch (config.side()) {
			case CLIENT -> "-client";
			case SERVER -> "-server";
			case COMMON -> "";
		};
	}

	private static <T> void registerIfNeeded (Class<T> clazz, String modid, ConfigManager<T> manager, T config, ConfigBuildOptions options) {
		ConfigBuildOptions resolvedOptions = options == null ? ConfigBuildOptions.defaults() : options;
		if (!resolvedOptions.shouldRegisterScreen()) return;

		Component title = resolvedOptions.screenTitle() != null ? resolvedOptions.screenTitle() : defaultTabTitle(clazz, modid);
		ConfigRegistry.register(modid, title, manager, config);
		Services.PLATFORM.registerConfigScreen(modid);
	}

	private static Component defaultTabTitle (Class<?> clazz, String modid) {
		Config config = clazz.getAnnotation(Config.class);
		String name = clazz.getSimpleName();
		if (name.endsWith("Config")) {
			name = name.substring(0, name.length() - "Config".length());
		}

		if (name.isBlank() || name.equalsIgnoreCase("Mod")) {
			name = config != null && config.side() != Side.COMMON ? sideName(config.side()) : modid;
		} else if (config != null && config.side() != Side.COMMON) {
			name = name + " " + sideName(config.side());
		}

		return Component.literal(humanize(name));
	}

	private static String sideName (Side side) {
		return switch (side) {
			case CLIENT -> "Client";
			case SERVER -> "Server";
			case COMMON -> "Common";
		};
	}

	private static String humanize (String value) {
		String normalized = value.replace('_', ' ').replace('-', ' ').trim();
		if (normalized.isBlank()) return value;

		StringBuilder result = new StringBuilder(normalized.length());
		boolean upperNext = true;
		for (int i = 0; i < normalized.length(); i++) {
			char c = normalized.charAt(i);
			if (Character.isWhitespace(c)) {
				result.append(c);
				upperNext = true;
			} else if (upperNext) {
				result.append(Character.toUpperCase(c));
				upperNext = false;
			} else {
				result.append(Character.toLowerCase(c));
			}
		}
		return result.toString();
	}

	/**
	 * Instantiates a new instance of the specified class that extends {@code AbstractFileType}.
	 * The class must have a no-argument constructor.
	 *
	 * @param cls The {@code Class} object representing the type of {@code AbstractFileType} to instantiate.
	 *            Must not be null and should have a public or accessible no-argument constructor.
	 * @return A new instance of the specified {@code AbstractFileType}.
	 * @throws IllegalStateException If instantiation fails due to missing or inaccessible constructor,
	 *                               or other reflection-related issues.
	 */
	private static AbstractFileType instantiateFileType (Class<? extends AbstractFileType> cls) {
		try {
			return cls.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to instantiate file type " + cls.getName(), e);
		}
	}

}
