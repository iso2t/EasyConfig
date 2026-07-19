package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

public class BooleanValue extends AbstractValue<Boolean> {
	public BooleanValue (boolean def) {
		super(def);
	}
	
	public static BooleanValue of (boolean def) {
		return new BooleanValue(def);
	}
	
}