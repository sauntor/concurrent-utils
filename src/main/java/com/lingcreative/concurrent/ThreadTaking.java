package com.lingcreative.concurrent;

import java.lang.annotation.*;

/**
 * A mark for a method (including {@code constructor} which will take over the execution time of the owner thread.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface ThreadTaking {

    /**
     * Note for the annotated element. Alias for {@link #note()}
     */
    String value() default "";

    /**
     * Note for the annotated element.
     */
    String note() default "";

}
