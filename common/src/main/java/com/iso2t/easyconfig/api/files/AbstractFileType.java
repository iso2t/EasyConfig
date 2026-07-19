package com.iso2t.easyconfig.api.files;

public abstract class AbstractFileType<T> {

	public abstract void write(T value);

	public abstract T read();

}
