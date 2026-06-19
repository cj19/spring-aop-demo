package org.darvasr.springaopdemo.poc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring-managed bean that holds the central {@link Handles} handler methods
 * in a single place. Because of the {@link Component} meta-annotation, component
 * scanning registers it as a bean automatically.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface GlobalExceptionHandler {
}
