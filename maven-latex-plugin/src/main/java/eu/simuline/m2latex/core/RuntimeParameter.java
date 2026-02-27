package eu.simuline.m2latex.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.maven.plugins.annotations.Mojo;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * For fields 
 * set whereever {@link Parameter} is set (in future automatically), 
 * to have a marker for parameters even at runtime. 
 * Parameters are private member fields. 
 * Note that {@link Parameter} itself is not visible at runtime. 
 * To generalize, 
 * it is also possible to endow methods without parameter with this annotation. 
 * That way, data can be read if statically available (fields)
 * or dynamically computed (methods). 
 * This annotation is used in {@link Mojo}s and in {@link Settings}. 
 * For use of {@link RuntimeParameter}s see {@link Settings#getProperties()}. 
 * Besides for {@link Settings#toString()} 
 * which use fields only, they go into injections by {@link Settings#getProperties()}. 
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Documented // to make the annotation occur in api docs 
@Target({ElementType.FIELD, ElementType.METHOD})
//public 
@interface RuntimeParameter {
  // marker annotation 
}
