package com.iso2t.easyconfig.api.metadata;

import java.util.List;
import java.util.Optional;

public final class ConfigSchema<T> {

	private final Class<T>          type;
	private final T                 config;
	private final List<ConfigEntry> entries;
	private final boolean           syncable;

	ConfigSchema (Class<T> type, T config, List<ConfigEntry> entries, boolean syncable) {
		this.type = type;
		this.config = config;
		this.entries = List.copyOf(entries);
		this.syncable = syncable;
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

	public boolean syncable () {
		return syncable;
	}

	public List<ConfigEntry> syncableEntries () {
		return entries.stream().filter(ConfigEntry::syncable).toList();
	}

	public List<ConfigEntry> sections () {
		return entries.stream().filter(entry -> entry.kind() == ConfigEntryKind.SECTION).toList();
	}

	public Optional<ConfigEntry> find (String path) {
		return entries.stream().filter(entry -> entry.path().equals(path)).findFirst();
	}

}
