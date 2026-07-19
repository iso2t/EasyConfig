package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

/**
 * Represents a configuration value that holds a boolean.
 * This class extends the {@code AbstractValue} class to manage boolean-typed configuration.
 * It provides a convenient method to create instances with a default value.
 * <p>
 * The stored value defaults to the provided value at the time of initialization.
 * Users can change or retrieve the value using methods inherited from {@code AbstractValue}.
 */
public class BooleanValue extends AbstractValue<Boolean> {

	private BooleanValue (boolean def) {
		super(def);
	}
	
	public static BooleanValue of (boolean def) {
		return new BooleanValue(def);
	}
	
}