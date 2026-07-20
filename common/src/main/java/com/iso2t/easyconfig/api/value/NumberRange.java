package com.iso2t.easyconfig.api.value;

/**
 * A generic interface for representing a range of numeric values.
 * This interface enforces methods to retrieve the minimum and
 * maximum values of the range.
 *
 * @param <T> the type of numeric value, which must extend {@code Number}
 */
public interface NumberRange<T extends Number> {

	T getMin ();

	T getMax ();

}
