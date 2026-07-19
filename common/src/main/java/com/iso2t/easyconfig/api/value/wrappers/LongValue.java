package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.NumberRange;

/**
 * A configuration value wrapper that represents a long value and its allowable range.
 * This class extends the {@code AbstractValue} class, providing additional functionality
 * for managing a range of valid long values.
 * <p>
 * This class implements the {@code NumberRange} interface to define constraints on the minimum
 * and maximum allowable long values.
 * <p>
 * The LongValue class provides factory methods to create instances with default values,
 * as well as with specific minimum and maximum bounds.
 */
public class LongValue extends AbstractValue<Long> implements NumberRange<Long> {

    private final Long min;
    private final Long max;

	private LongValue (Long def, Long min, Long max) {
        super(def);
        this.min = min;
        this.max = max;
    }

	private LongValue (Long def) {
		this(def, Long.MIN_VALUE, Long.MAX_VALUE);
	}

    @Override
    public Long getMin () {
        return min;
    }

    @Override
    public Long getMax () {
        return max;
    }
	
	public static LongValue of (Long def) {
		return new LongValue(def);
	}
	
	public static LongValue of (Long def, Long min, Long max) {
		return new LongValue(def, min, max);
	}

}

