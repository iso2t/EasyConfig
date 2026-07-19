package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

public class StringValue extends AbstractValue<String> {
	public StringValue (String def) {
		super(def);
	}
	
	public static StringValue of (String def) {
		return new StringValue(def);
	}
	
	public static StringValue of (CharSequence def) {
		return new StringValue(def.toString());
	}
	
}
