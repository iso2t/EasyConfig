package com.iso2t.easyconfig.api.value.comment;

import java.lang.reflect.Array;

final class CommentFormat {

	private CommentFormat () {
	}

	static String value (Object value) {
		if (value == null) return "null";
		if (value instanceof Character || value instanceof CharSequence) return "\"" + value + "\"";
		if (value.getClass().isArray()) return array(value);
		return String.valueOf(value);
	}

	static String type (Object value) {
		if (value == null) return "null";
		Class<?> type = value.getClass();
		if (type.isArray()) return type.getComponentType().getSimpleName() + "[]";
		return type.getSimpleName();
	}

	static int arrayLength (Object value) {
		if (value == null || !value.getClass().isArray()) return 0;
		return Array.getLength(value);
	}

	private static String array (Object value) {
		StringBuilder builder = new StringBuilder("[");
		int length = Array.getLength(value);
		for (int i = 0; i < length; i++) {
			if (i > 0) builder.append(", ");
			builder.append(value(Array.get(value, i)));
		}
		return builder.append("]").toString();
	}
}
