package com.iso2t.easyconfig.api.neoforge;

import com.iso2t.easyconfig.api.gui.ConfigScreens;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Objects;

public final class NeoForgeConfigScreens {

	private NeoForgeConfigScreens () {}

	public static boolean hasRegisteredConfigs (ModContainer modContainer) {
		Objects.requireNonNull(modContainer, "modContainer");
		return ConfigScreens.has(modContainer.getModId());
	}

	public static void register (ModContainer modContainer) {
		Objects.requireNonNull(modContainer, "modContainer");
		if (FMLEnvironment.getDist() != Dist.CLIENT) return;

		IConfigScreenFactory screenFactory = (container, parent) -> ConfigScreens.create(container.getModId(), parent)
			.map(Screen.class::cast)
			.orElse(parent);
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, screenFactory);
	}

}
