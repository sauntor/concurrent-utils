package com.lingcreative.concurrent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Documented
public @interface ThreadSafe {

    /**
     * Note for the annotated type.
     */
    String value() default "";

}
