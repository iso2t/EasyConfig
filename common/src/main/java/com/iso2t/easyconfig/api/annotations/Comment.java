package com.iso2t.easyconfig.api.annotations;

import com.iso2t.easyconfig.api.value.comment.AutoCommentValueProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.FIELD })
public @interface Comment {
    /**
     * Lines to emit above the property
     */
    String[] value () default {};

    /**
     * Whether value-derived comment lines should be emitted after {@link #value()}.
     */
    boolean values () default true;

    /**
     * The provider to use when {@link #values()} is enabled.
     */
    Class<? extends CommentValueProvider<?>> provider () default AutoCommentValueProvider.class;
}
