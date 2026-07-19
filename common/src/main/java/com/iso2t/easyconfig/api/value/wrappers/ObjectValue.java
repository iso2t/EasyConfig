package com.iso2t.easyconfig.api.value.wrappers;

import com.iso2t.easyconfig.api.value.AbstractValue;

public class ObjectValue<T> extends AbstractValue<T> {

    public ObjectValue (T def) {
        super(def);
    }

	public static <T> ObjectValue<T> of (T def) {
		return new ObjectValue<>(def);
	}

}
