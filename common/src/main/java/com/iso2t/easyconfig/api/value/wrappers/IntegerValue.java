package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.NumberRange;

/**
 * A configuration value wrapper that represents an integer value and its allowable range.
 * This class extends the {@code AbstractValue} class, providing functionality for
 * managing a range of valid integer values.
 */
public class IntegerValue extends AbstractValue<Integer> implements NumberRange<Integer> {

	private final Integer min;
	private final Integer max;

	private IntegerValue (Integer def, Integer min, Integer max) {
		super(def);
		this.min = min;
		this.max = max;
	}

	private IntegerValue (int def) {
		this(def, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public Integer getMin () {
		return min;
	}

	@Override
	public Integer getMax () {
		return max;
	}

	public static IntegerValue of (int def) {
		return new IntegerValue(def);
	}

	public static IntegerValue of (int def, int min, int max) {
		return new IntegerValue(def, min, max);
	}

}
