package com.iso2t.easyconfig.api.registry;

import com.iso2t.easyconfig.api.manager.ConfigManager;

public final class RegisteredConfig<T> {

	private final String           modId;
	private final String           key;
	private final String           title;
	private final ConfigManager<T> manager;
	private final T                config;

	RegisteredConfig (String modId, String key, String title, ConfigManager<T> manager, T config) {
		this.modId = modId;
		this.key = key;
		this.title = title;
		this.manager = manager;
		this.config = config;
	}

	public String modId () {
		return modId;
	}

	public String key () {
		return key;
	}

	public String title () {
		return title;
	}

	public ConfigManager<T> manager () {
		return manager;
	}

	public T config () {
		return config;
	}

}
