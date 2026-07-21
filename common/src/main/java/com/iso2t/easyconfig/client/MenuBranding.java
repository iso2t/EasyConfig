package com.iso2t.easyconfig.client;

import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.EasyConfig;
import com.iso2t.easyconfig.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;

public final class MenuBranding {

	private static final int TEXT_COLOR = 0xFFFFFFFF;

	private MenuBranding () {
	}

	public static void render (TitleScreen screen, GuiGraphicsExtractor graphics) {
		if (screen == null || graphics == null || EasyConfig.getConfig().SHOW_MAIN_MENU_BRANDING.get() == false) return;
		var font = Minecraft.getInstance().font;
		var isFabric = Services.PLATFORM.getPlatformName().equalsIgnoreCase("fabric");
		var children = Services.PLATFORM.getDependentModCount(Constants.MOD_ID);
		var text = String.format("%s (%s %s)", Constants.MOD_NAME, children, children == 1 ? "mod" : "mods");

		int x = 2;
		int y = screen.height - 10 - ((font.lineHeight + 1) * (isFabric ? 1 : 2));
		graphics.text(font, text, x, y, TEXT_COLOR);
	}

}
