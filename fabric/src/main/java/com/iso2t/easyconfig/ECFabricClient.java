package com.iso2t.easyconfig;

import com.iso2t.easyconfig.client.FabricMenuBranding;
import com.iso2t.easyconfig.client.FabricConfigScreenOpener;
import net.fabricmc.api.ClientModInitializer;

public class ECFabricClient implements ClientModInitializer {

	@Override
	public void onInitializeClient () {
		FabricMenuBranding.register();
		FabricConfigScreenOpener.register();
	}

}
