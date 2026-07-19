package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.NumberRange;

public class ByteValue extends AbstractValue<Byte> implements NumberRange<Byte> {

    private final Byte min;
    private final Byte max;

    public ByteValue (Byte def, Byte min, Byte max) {
        super(def);
        this.min = min;
        this.max = max;
    }

	public ByteValue (Byte def) {
		this(def, Byte.MIN_VALUE, Byte.MAX_VALUE);
	}

    @Override
    public Byte getMin () {
        return min;
    }

    @Override
    public Byte getMax () {
        return max;
    }
	
	public static ByteValue of (Byte def) {
		return new ByteValue(def);
	}
	
	public static ByteValue of (Byte def, Byte min, Byte max) {
		return new ByteValue(def, min, max);
	}
	
}

