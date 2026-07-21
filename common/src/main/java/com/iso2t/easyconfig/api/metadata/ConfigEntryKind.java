package com.iso2t.easyconfig.api.metadata;

public enum ConfigEntryKind {
	SECTION,
	BOOLEAN,
	NUMBER,
	COLOR,
	ENUM,
	STRING,
	CHARACTER,
	LIST,
	ARRAY,
	OBJECT,
	UNKNOWN;

	public boolean scalar () {
		return switch (this) {
			case BOOLEAN, NUMBER, COLOR, ENUM, STRING, CHARACTER -> true;
			case SECTION, LIST, ARRAY, OBJECT, UNKNOWN -> false;
		};
	}
}
