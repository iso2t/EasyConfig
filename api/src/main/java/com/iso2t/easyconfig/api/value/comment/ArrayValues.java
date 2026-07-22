package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

/**
 * Built-in {@link CommentValueProvider} for array-backed configuration fields.
 */
public final class ArrayValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		return new String[] { "Default: " + CommentFormat.value(defaultValue), "Length: " + CommentFormat.arrayLength(defaultValue) };
	}
}
