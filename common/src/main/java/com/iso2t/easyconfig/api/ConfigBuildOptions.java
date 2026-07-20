package com.iso2t.easyconfig.api;

import net.minecraft.network.chat.Component;

public final class ConfigBuildOptions {

	private static final ConfigBuildOptions DEFAULTS = new ConfigBuildOptions(true, null);

	private final boolean registerScreen;
	private final Component screenTitle;

	private ConfigBuildOptions (boolean registerScreen, Component screenTitle) {
		this.registerScreen = registerScreen;
		this.screenTitle = screenTitle;
	}

	public static ConfigBuildOptions defaults () {
		return DEFAULTS;
	}

	public static ConfigBuildOptions unregistered () {
		return DEFAULTS.registerScreen(false);
	}

	public ConfigBuildOptions registerScreen (boolean registerScreen) {
		return new ConfigBuildOptions(registerScreen, screenTitle);
	}

	public ConfigBuildOptions screenTitle (Component screenTitle) {
		return new ConfigBuildOptions(registerScreen, screenTitle);
	}

	public ConfigBuildOptions screenTitle (String screenTitle) {
		return screenTitle(Component.literal(screenTitle));
	}

	public boolean shouldRegisterScreen () {
		return registerScreen;
	}

	public Component screenTitle () {
		return screenTitle;
	}

}
