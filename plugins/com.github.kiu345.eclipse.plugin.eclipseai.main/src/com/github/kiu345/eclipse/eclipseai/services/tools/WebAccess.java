package com.github.kiu345.eclipse.eclipseai.services.tools;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method that uses web access
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface WebAccess {

}
