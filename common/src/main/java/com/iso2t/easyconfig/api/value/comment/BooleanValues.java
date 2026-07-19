package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

/**
 * Built-in {@link CommentValueProvider} implementation for boolean-backed configuration fields.
 *
 * This class dynamically generates comment lines for configuration fields of type boolean.
 *
 * <ul>
 *   <li>Comment line 1: {@code Default: <currentValue>}</li>
 *   <li>Comment line 2: {@code Allowed values: true, false}</li>
 * </ul>
 *
 * This implementation expects the default value of the field to be of type {@code Boolean}.
 */
public final class BooleanValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		return new String[] {
				"Default: " + CommentFormat.value(defaultValue),
				"Allowed values: true, false"
		};
	}
}
