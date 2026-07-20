package com.iso2t.easyconfig.api.metadata;

import com.iso2t.easyconfig.api.value.ConfigValue;
import com.iso2t.easyconfig.api.value.NumberRange;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

public final class ConfigEntry {

	private final List<String> path;
	private final String key;
	private final String displayName;
	private final ConfigEntryKind kind;
	private final Field field;
	private final Object owner;
	private final Object defaultOwner;
	private final Class<?> fieldType;
	private final Class<?> valueType;
	private final List<String> comments;
	private final List<Object> allowedValues;
	private final Object minValue;
	private final Object maxValue;
	private final boolean editable;

	ConfigEntry (
		List<String> path,
		String key,
		String displayName,
		ConfigEntryKind kind,
		Field field,
		Object owner,
		Object defaultOwner,
		Class<?> valueType,
		List<String> comments,
		List<Object> allowedValues,
		Object minValue,
		Object maxValue
	) {
		this.path = List.copyOf(path);
		this.key = key;
		this.displayName = displayName;
		this.kind = kind;
		this.field = field;
		this.owner = owner;
		this.defaultOwner = defaultOwner;
		this.fieldType = field.getType();
		this.valueType = valueType;
		this.comments = List.copyOf(comments);
		this.allowedValues = List.copyOf(allowedValues);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.editable = kind != ConfigEntryKind.SECTION && (ConfigValue.class.isAssignableFrom(fieldType) || !Modifier.isFinal(field.getModifiers()));
	}

	public List<String> pathSegments () {
		return path;
	}

	public String path () {
		return String.join(".", path);
	}

	public String key () {
		return key;
	}

	public String displayName () {
		return displayName;
	}

	public ConfigEntryKind kind () {
		return kind;
	}

	public Field field () {
		return field;
	}

	public Class<?> fieldType () {
		return fieldType;
	}

	public Class<?> valueType () {
		return valueType;
	}

	public List<String> comments () {
		return comments;
	}

	public List<Object> allowedValues () {
		return allowedValues;
	}

	public Object minValue () {
		return minValue;
	}

	public Object maxValue () {
		return maxValue;
	}

	public boolean editable () {
		return editable;
	}

	public boolean scalarEditable () {
		return editable && kind.scalar();
	}

	public Object value () {
		return readValue(owner, false);
	}

	public Object defaultValue () {
		return readValue(defaultOwner, true);
	}

	public ConfigValueResult validate (Object value) {
		if (!editable) {
			return ConfigValueResult.failure("Config entry " + path() + " is not editable");
		}

		if (value == null) {
			return ConfigValueResult.failure("Config entry " + path() + " does not accept null values");
		}

		return switch (kind) {
			case BOOLEAN -> convertBoolean(value);
			case NUMBER -> convertNumber(value);
			case ENUM -> convertEnum(value);
			case STRING -> ConfigValueResult.success(value.toString());
			case CHARACTER -> convertCharacter(value);
			case LIST, ARRAY, OBJECT, UNKNOWN -> convertAssignable(value);
			case SECTION -> ConfigValueResult.failure("Config entry " + path() + " is a section");
		};
	}

	public ConfigValueResult trySetValue (Object value) {
		ConfigValueResult result = validate(value);
		if (result.failed()) return result;

		writeValue(result.value());
		return result;
	}

	public void setValue (Object value) {
		ConfigValueResult result = trySetValue(value);
		if (result.failed()) {
			throw new IllegalArgumentException(result.message());
		}
	}

	public void resetValue () {
		setValue(defaultValue());
	}

	public ConfigValueResult tryResetValue () {
		return trySetValue(defaultValue());
	}

	private void writeValue (Object value) {
		if (!editable) {
			throw new IllegalStateException("Config entry " + path() + " is not editable");
		}

		try {
			Object raw = field.get(owner);
			if (raw instanceof ConfigValue<?> configValue) {
				@SuppressWarnings("unchecked") ConfigValue<Object> writable = (ConfigValue<Object>) configValue;
				writable.set(value);
				return;
			}
			field.set(owner, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to set config entry " + path(), e);
		}
	}

	public boolean hasRange () {
		return minValue != null || maxValue != null;
	}

	public boolean hasAllowedValues () {
		return !allowedValues.isEmpty();
	}

	private Object readValue (Object targetOwner, boolean defaultValue) {
		if (targetOwner == null) return null;

		try {
			Object raw = field.get(targetOwner);
			if (raw instanceof ConfigValue<?> configValue) {
				return defaultValue ? configValue.getDefault() : configValue.get();
			}
			return raw;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to read config entry " + path(), e);
		}
	}

	static Object minValue (Object raw) {
		return raw instanceof NumberRange<?> range ? range.getMin() : null;
	}

	static Object maxValue (Object raw) {
		return raw instanceof NumberRange<?> range ? range.getMax() : null;
	}

	private ConfigValueResult convertBoolean (Object value) {
		if (value instanceof Boolean bool) return ConfigValueResult.success(bool);

		String normalized = value.toString().trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "true", "yes", "y", "on", "1" -> ConfigValueResult.success(true);
			case "false", "no", "n", "off", "0" -> ConfigValueResult.success(false);
			default -> ConfigValueResult.failure("Expected a boolean value for " + path());
		};
	}

	private ConfigValueResult convertNumber (Object value) {
		Class<?> targetType = boxedNumberType();
		BigDecimal decimal = toDecimal(value);
		if (decimal == null) {
			return ConfigValueResult.failure("Expected a number for " + path());
		}

		ConfigValueResult rangeResult = validateRange(decimal);
		if (rangeResult.failed()) return rangeResult;

		try {
			if (targetType == Byte.class) return ConfigValueResult.success(decimal.byteValueExact());
			if (targetType == Short.class) return ConfigValueResult.success(decimal.shortValueExact());
			if (targetType == Integer.class) return ConfigValueResult.success(decimal.intValueExact());
			if (targetType == Long.class) return ConfigValueResult.success(decimal.longValueExact());
			if (targetType == Float.class) return ConfigValueResult.success(decimal.floatValue());
			if (targetType == Double.class) return ConfigValueResult.success(decimal.doubleValue());
			return ConfigValueResult.success(decimal);
		} catch (ArithmeticException e) {
			return ConfigValueResult.failure("Expected a " + targetType.getSimpleName() + " value for " + path());
		}
	}

	private ConfigValueResult convertEnum (Object value) {
		if (allowedValues.isEmpty()) {
			return ConfigValueResult.failure("No enum values are known for " + path());
		}

		for (Object allowedValue : allowedValues) {
			if (allowedValue == value || allowedValue.equals(value)) {
				return ConfigValueResult.success(allowedValue);
			}
		}

		String normalized = value.toString().trim();
		for (Object allowedValue : allowedValues) {
			Enum<?> enumValue = (Enum<?>) allowedValue;
			if (enumValue.name().equalsIgnoreCase(normalized) || enumValue.toString().equalsIgnoreCase(normalized)) {
				return ConfigValueResult.success(enumValue);
			}
		}

		return ConfigValueResult.failure("Expected one of " + allowedValues + " for " + path());
	}

	private ConfigValueResult convertCharacter (Object value) {
		if (value instanceof Character character) return ConfigValueResult.success(character);

		String text = value.toString();
		if (text.length() == 1) {
			return ConfigValueResult.success(text.charAt(0));
		}

		return ConfigValueResult.failure("Expected a single character for " + path());
	}

	private ConfigValueResult convertAssignable (Object value) {
		if (kind == ConfigEntryKind.ARRAY && value.getClass().isArray()) {
			return ConfigValueResult.success(value);
		}

		if (valueType.isInstance(value)) {
			return ConfigValueResult.success(value);
		}

		if (fieldType.isInstance(value)) {
			return ConfigValueResult.success(value);
		}

		return ConfigValueResult.failure("Expected " + valueType.getSimpleName() + " for " + path());
	}

	private Class<?> boxedNumberType () {
		Class<?> targetType = valueType;
		if (targetType == Object.class || targetType == Number.class) {
			Object current = value();
			if (current instanceof Number) targetType = current.getClass();
		}
		if (targetType == byte.class) return Byte.class;
		if (targetType == short.class) return Short.class;
		if (targetType == int.class) return Integer.class;
		if (targetType == long.class) return Long.class;
		if (targetType == float.class) return Float.class;
		if (targetType == double.class) return Double.class;
		if (Number.class.isAssignableFrom(targetType)) return targetType;
		return Double.class;
	}

	private ConfigValueResult validateRange (BigDecimal decimal) {
		BigDecimal min = toDecimal(minValue);
		if (min != null && decimal.compareTo(min) < 0) {
			return ConfigValueResult.failure("Value for " + path() + " must be at least " + minValue);
		}

		BigDecimal max = toDecimal(maxValue);
		if (max != null && decimal.compareTo(max) > 0) {
			return ConfigValueResult.failure("Value for " + path() + " must be at most " + maxValue);
		}

		return ConfigValueResult.success(decimal);
	}

	private static BigDecimal toDecimal (Object value) {
		if (value instanceof BigDecimal decimal) return decimal;
		if (value instanceof Number number) {
			if (number instanceof Double doubleValue && !Double.isFinite(doubleValue)) return null;
			if (number instanceof Float floatValue && !Float.isFinite(floatValue)) return null;
			try {
				return new BigDecimal(number.toString());
			} catch (NumberFormatException e) {
				return null;
			}
		}

		if (value instanceof CharSequence text) {
			String trimmed = text.toString().trim();
			if (trimmed.isEmpty()) return null;
			try {
				return new BigDecimal(trimmed);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		return null;
	}

}
