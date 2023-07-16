package eu.simuline.m2latex.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Set whereever {@link Parameter} is set (in future automatically), 
 * to have a marker even at runtime. 
 * Note that {@link Parameter} itself is not visible at runtime. 
 */
@Retention(value=RetentionPolicy.RUNTIME)
public @interface RuntimeParameter {
  // marker annotation 
}
