package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

/**
 * A configuration value wrapper that encapsulates an object of type T.
 * This class extends the {@code AbstractValue} class, allowing management
 * and manipulation of the value while maintaining a default value for fallback purposes.
 *
 * @param <T> the type of the encapsulated object
 */
public class ObjectValue<T> extends AbstractValue<T> {

	private ObjectValue (T def) {
		super(def);
	}

	public static <T> ObjectValue<T> of (T def) {
		return new ObjectValue<>(def);
	}

}
