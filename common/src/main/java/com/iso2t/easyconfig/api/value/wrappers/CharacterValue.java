package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

/**
 * A configuration value wrapper that represents a character value.
 * This class extends the {@code AbstractValue} class, providing functionality
 * to manage a default character value.
 */
public class CharacterValue extends AbstractValue<Character> {

	private CharacterValue (Character defaultValue) {
		super(defaultValue);
	}
	
	public static CharacterValue of (Character defaultValue) {
		return new CharacterValue(defaultValue);
	}
	
}
