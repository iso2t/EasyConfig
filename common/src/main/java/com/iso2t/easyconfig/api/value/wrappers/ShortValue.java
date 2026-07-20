package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.NumberRange;

/**
 * A configuration value wrapper that represents a short value and its allowable range.
 * This class extends the AbstractValue class, providing functionality for managing
 * a range of valid short values.
 * <p>
 * This class implements the NumberRange interface to define constraints on the minimum
 * and maximum allowable short values.
 * <p>
 * The ShortValue class provides factory methods to create instances with default values,
 * as well as with specific minimum and maximum bounds.
 */
public class ShortValue extends AbstractValue<Short> implements NumberRange<Short> {

	private final Short min;
	private final Short max;

	private ShortValue (Short def, Short min, Short max) {
		super(def);
		this.min = min;
		this.max = max;
	}

	private ShortValue (Short def) {
		this(def, Short.MIN_VALUE, Short.MAX_VALUE);
	}

	@Override
	public Short getMin () {
		return min;
	}

	@Override
	public Short getMax () {
		return max;
	}

	public static ShortValue of (Short def) {
		return new ShortValue(def);
	}

	public static ShortValue of (Short def, Short min, Short max) {
		return new ShortValue(def, min, max);
	}

}

