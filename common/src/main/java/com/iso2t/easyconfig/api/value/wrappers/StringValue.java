package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

/**
 * A configuration value wrapper that represents a string value.
 * This class extends the AbstractValue class, providing a mechanism for managing a default string value.
 * <p>
 * The StringValue class offers factory methods to create instances with default string values.
 * These instances can be initialized using either a {@code String} or a {@code CharSequence}.
 */
public class StringValue extends AbstractValue<String> {
	private StringValue (String def) {
		super(def);
	}

	public static StringValue of (String def) {
		return new StringValue(def);
	}

	public static StringValue of (CharSequence def) {
		return new StringValue(def.toString());
	}

}
