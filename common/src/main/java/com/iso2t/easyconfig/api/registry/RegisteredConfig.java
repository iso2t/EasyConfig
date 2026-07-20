package com.iso2t.easyconfig.api.registry;

import com.iso2t.easyconfig.api.gui.ConfigScreenTab;
import com.iso2t.easyconfig.api.manager.ConfigManager;
import net.minecraft.network.chat.Component;

public final class RegisteredConfig<T> {

	private final String modId;
	private final String key;
	private final Component title;
	private final ConfigManager<T> manager;
	private final T config;

	RegisteredConfig (String modId, String key, Component title, ConfigManager<T> manager, T config) {
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

	public Component title () {
		return title;
	}

	public ConfigManager<T> manager () {
		return manager;
	}

	public T config () {
		return config;
	}

	public ConfigScreenTab<T> asTab () {
		return ConfigScreenTab.of(title, manager, config);
	}

}
