package com.iso2t.easyconfig.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.TitleScreen;

public class FabricMenuBranding {

	public static void register () {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof TitleScreen titleScreen)) {
				return;
			}

			ScreenEvents.afterExtract(screen).register((renderedScreen, graphics, mouseX, mouseY, tickDelta) -> MenuBranding.render(titleScreen, graphics));
		});
	}

}
