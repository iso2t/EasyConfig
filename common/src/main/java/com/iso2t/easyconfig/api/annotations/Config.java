package com.iso2t.easyconfig.api.annotations;

import com.iso2t.easyconfig.api.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to define a configuration class. This annotation identifies
 * the annotated class as a configuration and provides metadata such as the name
 * and the applicable environment side (CLIENT, SERVER, or COMMON). It is commonly
 * used for auto-generating, managing, and validating configuration files.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
	/**
	 * Retrieves the name defined in the configuration annotation.
	 * This value is used as the identifier for the configuration class
	 * and can be utilized in constructing configuration file paths or identifiers.
	 *
	 * @return the name specified in the configuration annotation. If no name is provided,
	 * the behavior depends on the consuming context (e.g., fallback to a default value if applicable).
	 */
	String name ();

	/**
	 * Specifies the side (CLIENT, SERVER, or COMMON) for which the configuration is applicable.
	 * This can be used to differentiate configurations based on the environment in which they are deployed.
	 *
	 * @return the side associated with the configuration. The default is {@code Side.COMMON}.
	 */
	Side side () default Side.COMMON;
}
