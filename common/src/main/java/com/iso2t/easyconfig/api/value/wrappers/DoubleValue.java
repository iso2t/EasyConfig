package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.NumberRange;

public class DoubleValue extends AbstractValue<Double> implements NumberRange<Double> {

    private final Double min;
    private final Double max;

    public DoubleValue (Double def, Double min, Double max) {
        super(def);
        this.min = min;
        this.max = max;
    }

    public DoubleValue (Double def) {
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
