package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

/**
 * Implementation of the {@link CommentValueProvider} interface for general object-backed
 * configuration fields. This class dynamically generates two comment lines describing
 * the default value and its type.
 *
 * <ul>
 *   <li>The first comment line specifies the default value of the field
 *       by formatting it using {@link CommentFormat#value(Object)}.</li>
 *   <li>The second comment line specifies the data type of the default value
 *       using {@link CommentFormat#type(Object)}.</li>
 * </ul>
 *
 * This class is stateless, immutable, and thread-safe.
 */
public final class ObjectValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		return new String[] {
				"Default: " + CommentFormat.value(defaultValue),
				"Type: " + CommentFormat.type(defaultValue)
		};
	}
}
