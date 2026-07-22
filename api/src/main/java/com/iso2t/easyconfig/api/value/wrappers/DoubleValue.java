package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.NumberRange;

/**
 * Represents a configuration value wrapper for a Double type that includes default, minimum, and maximum values.
 * This class extends the AbstractValue base class for managing configuration values with a Double type
 * and implements the NumberRange interface to define a valid range for the value.
 * <p>
 * The DoubleValue provides constructors for initializing with a default value alone or with specified
 * minimum and maximum bounds. Static factory methods are available for convenient instantiation.
 */
public class DoubleValue extends AbstractValue<Double> implements NumberRange<Double> {

	private final Double min;
	private final Double max;

	private DoubleValue (Double def, Double min, Double max) {
		super(def);
		this.min = min;
		this.max = max;
	}

	private DoubleValue (Double def) {
		this(def, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	@Override
	public Double getMin () {
		return min;
	}

	@Override
	public Double getMax () {
		return max;
	}

	public static DoubleValue of (Double def) {
		return new DoubleValue(def);
	}

	public static DoubleValue of (Double def, Double min, Double max) {
		return new DoubleValue(def, min, max);
	}

}
