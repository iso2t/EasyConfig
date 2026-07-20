package com.iso2t.easyconfig.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public final class FabricConfigScreenOpener {

	private FabricConfigScreenOpener () {
	}

	public static void register () {
		KeyMappingHelper.registerKeyMapping(ConfigScreenOpener.keyMapping());
		ClientTickEvents.END_CLIENT_TICK.register(client -> ConfigScreenOpener.handleClientTick());
	}

}
