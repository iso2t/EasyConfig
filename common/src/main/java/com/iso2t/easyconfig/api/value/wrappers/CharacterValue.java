package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

public class CharacterValue extends AbstractValue<Character> {

	public CharacterValue (Character defaultValue) {
		super(defaultValue);
	}
	
	public static CharacterValue of (Character defaultValue) {
		return new CharacterValue(defaultValue);
	}
	
}
