package com.iso2t.easyconfig;

import com.iso2t.easyconfig.platform.Services;

public class EasyConfig {

    public static void init() {
		if (Services.PLATFORM.isDevelopmentEnvironment()) Constants.LOG.warn("EasyConfig is in development mode! Is this intentional?");
    }
}