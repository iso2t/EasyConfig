package com.iso2t.easyconfig.client;

import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.api.gui.ConfigScreens;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class ConfigScreenOpener {

	private static final String     OPEN_KEY           = "key.easyconfig.open_config_screen";
	private static final KeyMapping OPEN_CONFIG_SCREEN = new KeyMapping(OPEN_KEY, InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC);

	private ConfigScreenOpener () {
	}

	public static KeyMapping keyMapping () {
		return OPEN_CONFIG_SCREEN;
	}

	public static void handleClientTick () {
		while (OPEN_CONFIG_SCREEN.consumeClick()) {
			open(Constants.MOD_ID);
		}
	}

	public static boolean canOpen (String modId) {
		return ConfigScreens.has(modId);
	}

	public static boolean open (String modId) {
		Minecraft minecraft = Minecraft.getInstance();
		return ConfigScreens.create(modId, minecraft.gui.screen()).map(screen -> {
			minecraft.gui.setScreen(screen);
			return true;
		}).orElse(false);
	}

}
