package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

public final class StringValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		int length = defaultValue instanceof CharSequence text ? text.length() : 0;
		return new String[] {
				"Default: " + CommentFormat.value(defaultValue),
				"Length: " + length
		};
	}
}
