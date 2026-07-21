package com.iso2t.easyconfig.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to indicate that the annotated type or field should be ignored
 * in certain operations or processing. Commonly used in contexts where selective
 * inclusion or exclusion is necessary, such as configuration generation or reflection.
 * <p>
 * When applied to a class, all fields and nested elements of that class are ignored.
 * When applied to a field, only that specific field is excluded.
 * <p>
 * This annotation is typically processed at runtime due to its {@code RUNTIME} retention policy.
 * <p>
 * Applicable targets:
 * <ul>
 *     <li>{@code TYPE}: For ignoring an entire class.</li>
 *     <li>{@code FIELD}: For ignoring a specific field.</li>
 * </ul>
 *
 * @since 26.1.0.1
 * @author iso2t
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE, ElementType.FIELD })
public @interface Ignore {
}
