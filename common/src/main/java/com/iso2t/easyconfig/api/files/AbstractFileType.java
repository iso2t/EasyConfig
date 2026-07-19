package com.iso2t.easyconfig.api.files;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractFileType {
	private final String extension;

	public AbstractFileType (String extension) {
		this.extension = extension;
	}

	public String extension () {
		return extension;
	}

	public abstract ConfigNode read (Path file) throws IOException;

	public abstract void write (Path file, ConfigNode root) throws IOException;

	public abstract <T> T readValue (ConfigNode node, Class<T> type) throws IOException;

	public abstract <T> T convertValue (ConfigNode node, Class<T> type);
}
