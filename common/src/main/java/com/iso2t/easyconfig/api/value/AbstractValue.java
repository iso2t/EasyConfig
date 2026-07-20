package com.iso2t.easyconfig.api.value;

/**
 * Represents an abstract base class for configuration values that can hold a value of type {@code T}.
 * This class provides common functionality for getting, setting, and retrieving default values.
 *
 * @param <T> the type of value this configuration holds
 */
public abstract class AbstractValue<T> implements ConfigValue<T> {

	private       T value;
	private final T defaultValue;

	protected AbstractValue (T defaultValue) {
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	@Override
	public T get () {
		return value;
	}

	@Override
	public T getDefault () {
		return defaultValue;
	}

	@Override
	public void set (T v) {
		this.value = v;
	}
}
