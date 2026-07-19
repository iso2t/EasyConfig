package com.iso2t.easyconfig.client;

import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeMenuBranding {

	public static void register () {
		NeoForge.EVENT_BUS.addListener(NeoForgeMenuBranding::onScreenRender);
	}

	private static void onScreenRender (ScreenEvent.Render.Post event) {
		if (event.getScreen() instanceof TitleScreen titleScreen) {
			MenuBranding.render(titleScreen, event.getGuiGraphics());
		}
	}

}
