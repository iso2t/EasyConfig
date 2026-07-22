package com.iso2t.easyconfig.api.registry;

import com.iso2t.easyconfig.api.manager.ConfigManager;

import java.nio.file.Path;
import java.util.*;

public final class ConfigRegistry {

	private static final Map<String, List<RegisteredConfig<?>>> CONFIGS = new LinkedHashMap<>();

	private ConfigRegistry () {
	}

	public static synchronized <T> RegisteredConfig<T> register (String modId, String title, ConfigManager<T> manager, T config) {
		Objects.requireNonNull(modId, "modId");
		Objects.requireNonNull(title, "title");
		Objects.requireNonNull(manager, "manager");
		Objects.requireNonNull(config, "config");

		RegisteredConfig<T> registration = new RegisteredConfig<>(modId, key(manager), title, manager, config);
		List<RegisteredConfig<?>> configs = CONFIGS.computeIfAbsent(modId, ignored -> new ArrayList<>());

		for (int i = 0; i < configs.size(); i++) {
			if (configs.get(i).key().equals(registration.key())) {
				configs.set(i, registration);
				return registration;
			}
		}

		configs.add(registration);
		return registration;
	}

	public static synchronized List<RegisteredConfig<?>> get (String modId) {
		return List.copyOf(CONFIGS.getOrDefault(modId, List.of()));
	}

	public static synchronized Map<String, List<RegisteredConfig<?>>> all () {
		Map<String, List<RegisteredConfig<?>>> copy = new LinkedHashMap<>();
		CONFIGS.forEach((modId, configs) -> copy.put(modId, List.copyOf(configs)));
		return Collections.unmodifiableMap(copy);
	}

	public static synchronized boolean hasConfigs (String modId) {
		return CONFIGS.containsKey(modId) && !CONFIGS.get(modId).isEmpty();
	}

	public static synchronized boolean unregister (String modId, ConfigManager<?> manager) {
		List<RegisteredConfig<?>> configs = CONFIGS.get(modId);
		if (configs == null) return false;

		boolean removed = configs.removeIf(config -> config.key().equals(key(manager)));
		if (configs.isEmpty()) CONFIGS.remove(modId);
		return removed;
	}

	public static synchronized void clear () {
		CONFIGS.clear();
	}

	private static String key (ConfigManager<?> manager) {
		Path file = manager.file().toAbsolutePath().normalize();
		return file.toString();
	}

}
