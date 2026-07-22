package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

/**
 * A built-in {@link CommentValueProvider} implementation for fields backed by string-based default values.
 * <p>
 * Dynamically generates comment lines for configuration fields where the default value
 * is a {@link CharSequence}. This implementation emits two comment lines:
 * <p>
 * - The first line provides a string representation of the default value utilizing {@link CommentFormat#value(Object)}.
 * - The second line specifies the length of the string if the default value is a {@link CharSequence};
 * otherwise, the length is considered as {@code 0}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class StringValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		int length = defaultValue instanceof CharSequence text ? text.length() : 0;
		return new String[] { "Default: " + CommentFormat.value(defaultValue), "Length: " + length };
	}
}
