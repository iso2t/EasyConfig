package com.iso2t.easyconfig.api.value;

/**
 * Config values that use a different file representation than their runtime value.
 *
 * @param <T> runtime value type
 */
public interface SerializedConfigValue<T> extends ConfigValue<T> {

	Object serialized ();

	void deserialize (Object value);

}
