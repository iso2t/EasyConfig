package com.iso2t.easyconfig.api.metadata;

public final class ConfigValueResult {

	private final boolean success;
	private final Object value;
	private final String message;

	private ConfigValueResult (boolean success, Object value, String message) {
		this.success = success;
		this.value = value;
		this.message = message;
	}

	public static ConfigValueResult success (Object value) {
		return new ConfigValueResult(true, value, "");
	}

	public static ConfigValueResult failure (String message) {
		return new ConfigValueResult(false, null, message);
	}

	public boolean success () {
		return success;
	}

	public boolean failed () {
		return !success;
	}

	public Object value () {
		return value;
	}

	public String message () {
		return message;
	}

}
