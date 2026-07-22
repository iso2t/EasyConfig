package com.iso2t.easyconfig.api;

public final class ConfigBuildOptions {

	private static final ConfigBuildOptions DEFAULTS = new ConfigBuildOptions(true, null);

	private final boolean registerScreen;
	private final String  screenTitle;

	private ConfigBuildOptions (boolean registerScreen, String screenTitle) {
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

	public ConfigBuildOptions screenTitle (String screenTitle) {
		return new ConfigBuildOptions(registerScreen, screenTitle);
	}

	public boolean shouldRegisterScreen () {
		return registerScreen;
	}

	public String screenTitle () {
		return screenTitle;
	}

}
