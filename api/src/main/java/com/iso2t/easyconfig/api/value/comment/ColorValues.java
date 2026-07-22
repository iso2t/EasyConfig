package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;
import com.iso2t.easyconfig.api.value.wrappers.ColorValue;

import java.lang.reflect.Field;

public final class ColorValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		if (defaultValue instanceof Number number) {
			defaultValue = ColorValue.formatHex(number.intValue());
		}
		return new String[] { "Default: " + CommentFormat.value(defaultValue), "Format: #RRGGBB or #AARRGGBB" };
	}
}
