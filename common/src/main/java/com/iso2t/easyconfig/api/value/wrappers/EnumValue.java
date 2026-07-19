package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

public class EnumValue<E extends Enum<E>> extends AbstractValue<E> {

	private final Class<E> enumClass;

	/**
	 * @param enumClass  the enum type, e.g. MyMode.class
	 * @param defaultVal the default enum constant, e.g. MyMode.FOO
	 */
	private EnumValue (Class<E> enumClass, E defaultVal) {
		super(defaultVal);
		this.enumClass = enumClass;
	}

	private EnumValue (E defaultVal) {
		this(defaultVal.getDeclaringClass(), defaultVal);
	}

	/**
	 * helper to turn a string into the enum constant
	 */
	public void set (String name) {
		E e = Enum.valueOf(enumClass, name);
		super.set(e);
	}
	
	public static <E extends Enum<E>> EnumValue<E> of (E defaultValue) {
		return new EnumValue<>(defaultValue);
	}
	
	public static <E extends Enum<E>> EnumValue<E> of (Class<E> enumClass, E defaultValue) {
		return new EnumValue<>(enumClass, defaultValue);
	}
}
