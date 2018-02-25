package com.lingcreative.concurrent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
public @interface BlockedLikely {

    /**
     * Note for the annotated element. Alias for {@link #note()}
     */
    String value() default "";

    /**
     * Note for the annotated element.
     */
    String note() default "";

}
