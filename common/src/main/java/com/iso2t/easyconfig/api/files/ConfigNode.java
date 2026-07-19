package com.iso2t.easyconfig.api.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConfigNode {
	public enum Type {
		OBJECT,
		ARRAY,
		VALUE,
		NULL
	}

	private static final ConfigNode NULL = new ConfigNode(Type.NULL, null, null, null);

	private final Type type;
	private final Map<String, Entry> entries;
	private final List<ConfigNode> elements;
	private final Object value;

	private ConfigNode (Type type, Map<String, Entry> entries, List<ConfigNode> elements, Object value) {
		this.type = type;
		this.entries = entries;
		this.elements = elements;
		this.value = value;
	}

	public static ConfigNode object () {
		return new ConfigNode(Type.OBJECT, new LinkedHashMap<>(), null, null);
	}

	public static ConfigNode array () {
		return new ConfigNode(Type.ARRAY, null, new ArrayList<>(), null);
	}

	public static ConfigNode value (Object value) {
		if (value == null) return nullNode();
		return new ConfigNode(Type.VALUE, null, null, value);
	}

	public static ConfigNode nullNode () {
		return NULL;
	}

	public Type type () {
		return type;
	}

	public boolean isObject () {
		return type == Type.OBJECT;
	}

	public boolean isArray () {
		return type == Type.ARRAY;
	}

	public boolean isValue () {
		return type == Type.VALUE;
	}

	public boolean isNull () {
		return type == Type.NULL;
	}

	public void put (String key, ConfigNode node) {
		put(key, node, List.of());
	}

	public void put (String key, ConfigNode node, Collection<String> comments) {
		require(Type.OBJECT);
		entries.put(Objects.requireNonNull(key, "key"), new Entry(key, node == null ? nullNode() : node, comments));
	}

	public ConfigNode get (String key) {
		if (!isObject()) return nullNode();
		Entry entry = entries.get(key);
		if (entry == null) return nullNode();
		return entry.value();
	}

	public Collection<Entry> entries () {
		require(Type.OBJECT);
		return Collections.unmodifiableCollection(entries.values());
	}

	public void add (ConfigNode node) {
		require(Type.ARRAY);
		elements.add(node == null ? nullNode() : node);
	}

	public List<ConfigNode> elements () {
		require(Type.ARRAY);
		return Collections.unmodifiableList(elements);
	}

	public Object value () {
		require(Type.VALUE);
		return value;
	}

	public Object rawValue () {
		return switch (type) {
			case OBJECT -> {
				Map<String, Object> raw = new LinkedHashMap<>();
				for (Entry entry : entries.values()) {
					raw.put(entry.key(), entry.value().rawValue());
				}
				yield raw;
			}
			case ARRAY -> {
				List<Object> raw = new ArrayList<>();
				for (ConfigNode element : elements) {
					raw.add(element.rawValue());
				}
				yield raw;
			}
			case VALUE -> value;
			case NULL -> null;
		};
	}

	private void require (Type requiredType) {
		if (type != requiredType) {
			throw new IllegalStateException("Expected " + requiredType + " config node, got " + type);
		}
	}

	public static final class Entry {
		private final String key;
		private final ConfigNode value;
		private final List<String> comments;

		private Entry (String key, ConfigNode value, Collection<String> comments) {
			this.key = key;
			this.value = value;
			this.comments = copyComments(comments);
		}

		public String key () {
			return key;
		}

		public ConfigNode value () {
			return value;
		}

		public List<String> comments () {
			return comments;
		}

		private static List<String> copyComments (Collection<String> comments) {
			if (comments == null || comments.isEmpty()) return List.of();

			List<String> copy = new ArrayList<>();
			for (String comment : comments) {
				if (comment != null) copy.add(comment);
			}
			return List.copyOf(copy);
		}
	}
}
