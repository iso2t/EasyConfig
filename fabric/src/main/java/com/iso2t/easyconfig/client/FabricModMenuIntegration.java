package com.iso2t.easyconfig.client;

import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.api.gui.ConfigScreens;
import com.iso2t.easyconfig.api.registry.ConfigRegistry;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FabricModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory () {
		return screenFactory(Constants.MOD_ID);
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories () {
		Map<String, ConfigScreenFactory<?>> factories = new LinkedHashMap<>();
		ConfigRegistry.all().keySet().stream().filter(modId -> !Constants.MOD_ID.equals(modId)).forEach(modId -> factories.put(modId, screenFactory(modId)));
		return factories;
	}

	private static ConfigScreenFactory<Screen> screenFactory (String modId) {
		return parent -> ConfigScreens.create(modId, parent).map(Screen.class::cast).orElse(parent);
	}

}
