package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

public final class BooleanValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		return new String[] {
				"Default: " + CommentFormat.value(defaultValue),
				"Allowed values: true, false"
		};
	}
}
