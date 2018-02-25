package com.lingcreative.concurrent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ThreadSafeRequired {

    /**
     * Reason or not for why thread safety is required.
     */
    String value() default "";

}
