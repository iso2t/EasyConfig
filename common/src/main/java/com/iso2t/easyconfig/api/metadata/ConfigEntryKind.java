package com.iso2t.easyconfig.api.metadata;

public enum ConfigEntryKind {
	SECTION,
	BOOLEAN,
	NUMBER,
	ENUM,
	STRING,
	CHARACTER,
	LIST,
	ARRAY,
	OBJECT,
	UNKNOWN;

	public boolean scalar () {
		return switch (this) {
			case BOOLEAN, NUMBER, ENUM, STRING, CHARACTER -> true;
			case SECTION, LIST, ARRAY, OBJECT, UNKNOWN -> false;
		};
	}
}
