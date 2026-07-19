package com.iso2t.easyconfig.api.files;

public enum FileTypes {
	JSON5(Json5.class);

	private final Class<? extends AbstractFileType> type;

	FileTypes (Class<? extends AbstractFileType> type) {
		this.type = type;
	}

	public AbstractFileType create () {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to instantiate file type " + type.getName(), e);
		}
	}

	public Class<? extends AbstractFileType> type () {
		return type;
	}

	public String extension () {
		return create().extension();
	}
}
