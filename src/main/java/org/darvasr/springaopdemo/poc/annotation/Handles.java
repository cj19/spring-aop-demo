package org.darvasr.springaopdemo.poc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a handler method on a {@link GlobalExceptionHandler} bean, declaring the
 * exception type it handles. The aspect selects the handler method that matches the
 * thrown exception type most specifically.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Handles {

    /** The handled exception type (or its subtypes). */
    Class<? extends Throwable> value();
}
