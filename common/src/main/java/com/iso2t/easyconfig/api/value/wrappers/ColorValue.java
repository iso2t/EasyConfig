package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.SerializedConfigValue;

/**
 * ARGB color value serialized as a hex string.
 */
public class ColorValue extends AbstractValue<Integer> implements SerializedConfigValue<Integer> {

	private ColorValue (int def) {
		super(def);
	}

	public static ColorValue of (int def) {
		return new ColorValue(def);
	}

	public static ColorValue of (String def) {
		return new ColorValue(parseHex(def));
	}

	public String hex () {
		return formatHex(get());
	}

	public String defaultHex () {
		return formatHex(getDefault());
	}

	public void setHex (String hex) {
		set(parseHex(hex));
	}

	@Override
	public Object serialized () {
		return hex();
	}

	@Override
	public void deserialize (Object value) {
		if (value instanceof Number number) {
			set(number.intValue());
			return;
		}
		setHex(String.valueOf(value));
	}

	public static String formatHex (int color) {
		return String.format("#%08X", color);
	}

	public static int parseHex (String hex) {
		if (hex == null) {
			throw new IllegalArgumentException("Color hex cannot be null");
		}

		String normalized = hex.trim();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}

		if (normalized.length() == 6) {
			normalized = "FF" + normalized;
		}

		if (normalized.length() != 8 || !normalized.matches("[0-9a-fA-F]{8}")) {
			throw new IllegalArgumentException("Expected #RRGGBB or #AARRGGBB");
		}

		return (int) Long.parseUnsignedLong(normalized, 16);
	}

}
