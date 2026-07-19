package com.iso2t.easyconfig.api.value.comment;

import com.iso2t.easyconfig.api.annotations.CommentValueProvider;

import java.lang.reflect.Field;

/**
 * A built-in {@link CommentValueProvider} implementation for character-based
 * configuration fields or fields that can be treated as character sequences.
 *
 * <p>Generates a single-line comment for configuration fields:
 * - {@code Default: <defaultValue>}
 *
 * <p>The default value is formatted using {@link CommentFormat#value(Object)} to ensure
 * proper representation of characters and strings, such as enclosing character or string
 * values in double quotes.
 */
public final class CharacterValues implements CommentValueProvider<Object> {

	@Override
	public String[] getCommentLines (Field field, Object defaultValue) {
		return new String[] {
				"Default: " + CommentFormat.value(defaultValue)
		};
	}
}
