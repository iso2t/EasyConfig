package com.iso2t.easyconfig.platform;

import com.iso2t.easyconfig.platform.services.IPlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class Services {
	private static final Logger LOG = LoggerFactory.getLogger("EasyConfig Services");
	public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

	public static <T> T load (Class<T> clazz) {

		final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader()).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
		LOG.debug("Loaded {} for service {}", loadedService, clazz);
		return loadedService;
	}
}
