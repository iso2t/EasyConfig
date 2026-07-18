package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

public class ArrayValue<T> extends AbstractValue<T[]> {

	public ArrayValue (T[] def) {
		super(def);
	}

	@Override
	public T[] get () {
		return super.get();
	}

	@Override
	public void set (T[] newValue) {
		super.set(newValue);
	}

	public T getIndex (int index) {
		return get()[index];
	}

	public T getFirst () {
		return get()[0];
	}

	public T getLast () {
		return get()[get().length - 1];
	}

}
