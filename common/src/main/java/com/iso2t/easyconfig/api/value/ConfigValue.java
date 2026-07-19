package com.iso2t.easyconfig.api.value;

import java.util.function.Supplier;

/**
 * Represents a configuration value that can hold and manage a value of type {@code T}.
 * This interface provides methods for accessing, modifying, and retrieving the default value.
 *
 * @param <T> the type of the configuration value
 */
public interface ConfigValue<T> extends Supplier<T> {
	/**
	 * The default value, used if none present on load
	 */
	T getDefault ();

	/**
	 * Set a new value
	 */
	void set (T value);
}
