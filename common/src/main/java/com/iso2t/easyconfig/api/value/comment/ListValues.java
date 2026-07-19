package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;
import java.util.Collection;

public final class ListValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		int size = defaultValue instanceof Collection<?> collection ? collection.size() : 0;
		return new String[] {
				"Default: " + CommentFormat.value(defaultValue),
				"Size: " + size
		};
	}
}
