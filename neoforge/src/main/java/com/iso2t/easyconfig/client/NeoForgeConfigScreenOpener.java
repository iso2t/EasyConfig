package com.iso2t.easyconfig.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class NeoForgeConfigScreenOpener {

	private NeoForgeConfigScreenOpener () {}

	public static void register (IEventBus modEventBus) {
		modEventBus.addListener(NeoForgeConfigScreenOpener::registerKeyMappings);
		NeoForge.EVENT_BUS.addListener(NeoForgeConfigScreenOpener::onClientTick);
	}

	private static void registerKeyMappings (RegisterKeyMappingsEvent event) {
		event.register(ConfigScreenOpener.keyMapping());
	}

	private static void onClientTick (ClientTickEvent.Post event) {
		ConfigScreenOpener.handleClientTick();
	}

}
