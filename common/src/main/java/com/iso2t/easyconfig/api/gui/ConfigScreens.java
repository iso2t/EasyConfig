package com.iso2t.easyconfig.api.gui;

import com.iso2t.easyconfig.api.registry.ConfigRegistry;
import com.iso2t.easyconfig.api.registry.RegisteredConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ConfigScreens {

	private ConfigScreens () {
	}

	public static boolean has (String modId) {
		return ConfigRegistry.hasConfigs(modId);
	}

	public static Optional<ConfigScreen> create (String modId, Screen parent) {
		return create(modId, parent, Component.translatable("easyconfig.config_screen.title", modId));
	}

	public static Optional<ConfigScreen> create (String modId, Screen parent, Component title) {
		List<ConfigScreenTab<?>> tabs = new ArrayList<>();
		for (RegisteredConfig<?> registration : ConfigRegistry.get(modId)) {
			tabs.add(registration.asTab());
		}

		if (tabs.isEmpty()) return Optional.empty();
		return Optional.of(new ConfigScreen(parent, title, tabs));
	}

	public static ConfigScreen createOrThrow (String modId, Screen parent) {
		return createOrThrow(modId, parent, Component.translatable("easyconfig.config_screen.title", modId));
	}

	public static ConfigScreen createOrThrow (String modId, Screen parent, Component title) {
		return create(modId, parent, title).orElseThrow(() -> new IllegalStateException("No configs registered for " + modId));
	}

}
