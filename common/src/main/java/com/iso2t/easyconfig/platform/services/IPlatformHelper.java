package com.iso2t.easyconfig.platform.services;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {

	/**
	 * Gets the name of the current platform
	 *
	 * @return The name of the current platform.
	 */
	String getPlatformName ();

	/**
	 * Checks if a mod with the given id is loaded.
	 *
	 * @param modId The mod to check if it is loaded.
	 * @return True if the mod is loaded, false otherwise.
	 */
	boolean isModLoaded (String modId);

	/**
	 * Gets loaded mods that declare the given mod id as a required dependency.
	 *
	 * @param modId The mod id to check dependents for.
	 * @return Loaded mod ids that require the given mod.
	 */
	List<String> getDependentMods (String modId);

	/**
	 * Counts loaded mods that declare the given mod id as a required dependency.
	 *
	 * @param modId The mod id to check dependents for.
	 * @return Number of loaded mods that require the given mod.
	 */
	default int getDependentModCount (String modId) {
		return getDependentMods(modId).size();
	}

	/**
	 * Registers the platform-native config screen entry point for the given mod id, if supported.
	 *
	 * @param modId The mod id whose registered EasyConfig configs should be exposed.
	 * @return True if the platform registered a native config screen entry point.
	 */
	default boolean registerConfigScreen (String modId) {
		return false;
	}

	/**
	 * Check if the game is currently in a development environment.
	 *
	 * @return True if in a development environment, false otherwise.
	 */
	boolean isDevelopmentEnvironment ();

	/**
	 * Gets the name of the environment type as a string.
	 *
	 * @return The name of the environment type.
	 */
	default String getEnvironmentName () {
		return isDevelopmentEnvironment() ? "development" : "production";
	}

	/**
	 * Gets the config directory for the current platform.
	 *
	 * @return The config directory path.
	 */
	Path getConfigDir ();

	/**
	 * Gets the mods directory for the current platform.
	 *
	 * @return The mods directory path.
	 */
	Path getModsDir ();

	/**
	 * Gets the game directory for the current platform.
	 *
	 * @return The game directory path.
	 */
	Path getGameDir ();

	/**
	 * Determines whether the current platform is NeoForge.
	 *
	 * @return True if the platform is identified as "neoforge", false otherwise.
	 */
	default boolean isNeoForge () {
		return getPlatformName().equals("neoforge");
	}

	/**
	 * Determines whether the current platform is Fabric.
	 *
	 * @return True if the platform is identified as "fabric", false otherwise.
	 */
	default boolean isFabric () {
		return getPlatformName().equals("fabric");
	}
}
