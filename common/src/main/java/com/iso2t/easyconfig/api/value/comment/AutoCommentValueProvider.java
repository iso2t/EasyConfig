package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

/**
 * A marker class used by {@link Comment#provider()} to indicate that the
 * {@link CommentValueProvider} should be automatically detected based on the
 * field's type.
 *
 * <p>This provider does not produce any lines itself; it is only a marker.
 */
public final class AutoCommentValueProvider implements CommentValueProvider<Object> {
	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		return new String[0];
	}
}
