package com.iso2t.easyconfig.api.metadata;

import com.iso2t.easyconfig.api.reflect.ConfigReflection;
import com.iso2t.easyconfig.api.value.ConfigValue;
import com.iso2t.easyconfig.api.value.NumberRange;
import com.iso2t.easyconfig.api.value.wrappers.*;

import java.lang.reflect.Field;
import java.util.*;

public final class ConfigIntrospector {

	private ConfigIntrospector () {
	}

	public static <T> ConfigSchema<T> inspect (Class<T> type) {
		return inspect(ConfigReflection.instantiate(type));
	}

	public static <T> ConfigSchema<T> inspect (T config) {
		Objects.requireNonNull(config, "config");

		@SuppressWarnings("unchecked") Class<T> type = (Class<T>) config.getClass();
		T defaultConfig = ConfigReflection.instantiate(type);

		List<ConfigEntry> entries = new ArrayList<>();
		collect(config, defaultConfig, List.of(), entries);
		return new ConfigSchema<>(type, config, entries);
	}

	private static void collect (Object owner, Object defaultOwner, List<String> parentPath, List<ConfigEntry> entries) {
		for (Field field : ConfigReflection.configFields(owner.getClass())) {
			String key = field.getName().toLowerCase(Locale.ROOT);
			List<String> path = append(parentPath, key);

			Object raw = readField(field, owner);
			Object defaultRaw = defaultOwner == null ? null : readField(field, defaultOwner);

			if (ConfigReflection.isNestedConfig(field.getType())) {
				Object nested = ensureNested(field, owner, raw);
				Object defaultNested = defaultOwner == null ? null : ensureNested(field, defaultOwner, defaultRaw);
				entries.add(entry(path, key, field, owner, defaultOwner, nested, ConfigEntryKind.SECTION));
				collect(nested, defaultNested, path, entries);
				continue;
			}

			entries.add(entry(path, key, field, owner, defaultOwner, raw, kind(field, raw)));
		}
	}

	private static ConfigEntry entry (List<String> path, String key, Field field, Object owner, Object defaultOwner, Object raw, ConfigEntryKind kind) {
		return new ConfigEntry(path, key, displayName(field.getName()), kind, field, owner, defaultOwner, valueType(field, raw), ConfigReflection.collectComments(field, owner), allowedValues(field, raw), ConfigEntry.minValue(raw), ConfigEntry.maxValue(raw));
	}

	private static ConfigEntryKind kind (Field field, Object raw) {
		Class<?> fieldType = field.getType();
		Object value = raw instanceof ConfigValue<?> configValue ? configValue.get() : raw;

		if (BooleanValue.class.isAssignableFrom(fieldType) || fieldType == Boolean.class || fieldType == boolean.class || value instanceof Boolean) return ConfigEntryKind.BOOLEAN;
		if (ColorValue.class.isAssignableFrom(fieldType) || raw instanceof ColorValue) return ConfigEntryKind.COLOR;
		if (NumberRange.class.isAssignableFrom(fieldType) || value instanceof Number) return ConfigEntryKind.NUMBER;
		if (EnumValue.class.isAssignableFrom(fieldType) || fieldType.isEnum() || value instanceof Enum<?>) return ConfigEntryKind.ENUM;
		if (StringValue.class.isAssignableFrom(fieldType) || CharSequence.class.isAssignableFrom(fieldType) || value instanceof CharSequence) return ConfigEntryKind.STRING;
		if (CharacterValue.class.isAssignableFrom(fieldType) || fieldType == Character.class || fieldType == char.class || value instanceof Character) return ConfigEntryKind.CHARACTER;
		if (ListValue.class.isAssignableFrom(fieldType) || Collection.class.isAssignableFrom(fieldType) || value instanceof Collection<?>) return ConfigEntryKind.LIST;
		if (ArrayValue.class.isAssignableFrom(fieldType) || fieldType.isArray() || value != null && value.getClass().isArray()) return ConfigEntryKind.ARRAY;
		if (ObjectValue.class.isAssignableFrom(fieldType)) return ConfigEntryKind.OBJECT;
		if (raw != null) return ConfigEntryKind.OBJECT;
		return ConfigEntryKind.UNKNOWN;
	}

	private static Class<?> valueType (Field field, Object raw) {
		Object value = raw instanceof ConfigValue<?> configValue ? configValue.get() : raw;
		Class<?> inferred = ConfigReflection.inferValueType(field);
		if (inferred != Object.class && inferred != Enum.class) return inferred;
		if (value != null) return value.getClass();
		return inferred;
	}

	private static List<Object> allowedValues (Field field, Object raw) {
		Class<?> valueType = valueType(field, raw);
		if (valueType.isEnum()) {
			return Arrays.asList(valueType.getEnumConstants());
		}
		return List.of();
	}

	private static Object ensureNested (Field field, Object owner, Object raw) {
		if (raw != null) return raw;

		Object nested = ConfigReflection.instantiate(field.getType());
		try {
			field.set(owner, nested);
			return nested;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to initialize nested config field " + field.getName(), e);
		}
	}

	private static Object readField (Field field, Object owner) {
		try {
			return field.get(owner);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to read config field " + field.getName(), e);
		}
	}

	private static List<String> append (List<String> path, String key) {
		List<String> copy = new ArrayList<>(path);
		copy.add(key);
		return List.copyOf(copy);
	}

	private static String displayName (String fieldName) {
		String normalized = fieldName.toLowerCase(Locale.ROOT).replace('_', ' ');
		StringBuilder displayName = new StringBuilder(normalized.length());
		boolean upperNext = true;
		for (int i = 0; i < normalized.length(); i++) {
			char c = normalized.charAt(i);
			if (Character.isWhitespace(c)) {
				displayName.append(c);
				upperNext = true;
			} else if (upperNext) {
				displayName.append(Character.toUpperCase(c));
				upperNext = false;
			} else {
				displayName.append(c);
			}
		}
		return displayName.toString();
	}

}
