package com.iso2t.easyconfig;

import net.fabricmc.api.ModInitializer;

public class ECFabric implements ModInitializer {

	@Override
	public void onInitialize () {
		EasyConfig.init();
	}
}
