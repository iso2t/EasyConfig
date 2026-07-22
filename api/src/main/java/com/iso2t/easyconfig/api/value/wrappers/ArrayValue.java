package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

/**
 * Represents a configuration value that holds an array of elements of type T.
 * This class extends the AbstractValue class to manage array-based configuration.
 *
 * @param <T> the type of elements in the array
 */
public class ArrayValue<T> extends AbstractValue<T[]> {

	private ArrayValue (T[] def) {
		super(def);
	}

	@Override
	public T[] get () {
		return super.get();
	}

	@Override
	public void set (T[] newValue) {
		super.set(newValue);
	}

	public T getIndex (int index) {
		return get()[index];
	}

	public T getFirst () {
		return get()[0];
	}

	public T getLast () {
		return get()[get().length - 1];
	}

	public static <T> ArrayValue<T> of (T... values) {
		return new ArrayValue<>(values);
	}

}
