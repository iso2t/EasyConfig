package com.iso2t.easyconfig.api.metadata;

import java.util.List;
import java.util.Optional;

public final class ConfigSchema<T> {

	private final Class<T>          type;
	private final T                 config;
	private final List<ConfigEntry> entries;

	ConfigSchema (Class<T> type, T config, List<ConfigEntry> entries) {
		this.type = type;
		this.config = config;
		this.entries = List.copyOf(entries);
	}

	public Class<T> type () {
		return type;
	}

	public T config () {
		return config;
	}

	public List<ConfigEntry> entries () {
		return entries;
	}

	public List<ConfigEntry> editableEntries () {
		return entries.stream().filter(ConfigEntry::editable).toList();
	}

	public List<ConfigEntry> sections () {
		return entries.stream().filter(entry -> entry.kind() == ConfigEntryKind.SECTION).toList();
	}

	public Optional<ConfigEntry> find (String path) {
		return entries.stream().filter(entry -> entry.path().equals(path)).findFirst();
	}

}
