package com.iso2t.easyconfig.api;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public final class ConfigPlatform {

	private static Path             configDir = Path.of("config");
	private static Consumer<String> screenRegistrar = ignored -> {
	};

	private ConfigPlatform () {
	}

	public static void configure (Path configDir, Consumer<String> screenRegistrar) {
		ConfigPlatform.configDir = Objects.requireNonNull(configDir, "configDir");
		ConfigPlatform.screenRegistrar = Objects.requireNonNull(screenRegistrar, "screenRegistrar");
	}

	public static Path configDir () {
		return configDir;
	}

	public static void registerConfigScreen (String modId) {
		screenRegistrar.accept(modId);
	}
}
