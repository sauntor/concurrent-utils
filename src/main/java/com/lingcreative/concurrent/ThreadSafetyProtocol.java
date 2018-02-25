package com.lingcreative.concurrent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ThreadSafetyProtocol {

    /**
     * Reason or not for why thread safety is required. Alias for {@link #note()}
     */
    String value() default "";

    /**
     * Says whether the annotated element is required of the thread safety.
     */
    boolean concurrentRequired() default true;

    /**
     * Reason or not for why thread safety is required.
     */
    String note() default "";

}
