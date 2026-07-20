package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.NumberRange;

/**
 * A configuration value wrapper that represents a float value and its allowable range.
 * This class extends the AbstractValue class, providing additional functionality
 * for managing a range of valid float values.
 * <p>
 * This class implements the NumberRange interface to define constraints on the minimum
 * and maximum allowable float values.
 * <p>
 * The FloatValue class provides factory methods to create instances with default values,
 * as well as with specific minimum and maximum bounds.
 */
public class FloatValue extends AbstractValue<Float> implements NumberRange<Float> {

	private final Float min;
	private final Float max;

	private FloatValue (Float def, Float min, Float max) {
		super(def);
		this.min = min;
		this.max = max;
	}

	private FloatValue (Float def) {
		this(def, Float.MIN_VALUE, Float.MAX_VALUE);
	}

	@Override
	public Float getMin () {
		return min;
	}

	@Override
	public Float getMax () {
		return max;
	}

	public static FloatValue of (Float def) {
		return new FloatValue(def);
	}

	public static FloatValue of (Float def, Float min, Float max) {
		return new FloatValue(def, min, max);
	}

}

