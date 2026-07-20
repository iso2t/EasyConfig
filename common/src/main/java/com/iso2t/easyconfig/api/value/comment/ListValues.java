package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Built-in {@link CommentValueProvider} implementation for fields backed by collections.
 * <p>
 * This class dynamically generates comment lines for configuration fields with default values
 * of type {@link Collection}.
 * <p>
 * The first line provides a string representation of the default value using {@link CommentFormat#value(Object)}.
 * The second line indicates the size of the collection. If the default value is not a collection,
 * the size is considered as {@code 0}.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ListValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		int size = defaultValue instanceof Collection<?> collection ? collection.size() : 0;
		return new String[] { "Default: " + CommentFormat.value(defaultValue), "Size: " + size };
	}
}
