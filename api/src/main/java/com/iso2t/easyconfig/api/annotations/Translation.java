package com.iso2t.easyconfig.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Translation keys for a generated control or nested section. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Translation {

	String value ();

	String tooltip () default "";

	/** Prefix followed by the lowercase enum constant name. */
	String valuePrefix () default "";
}
