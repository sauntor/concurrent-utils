package com.lingcreative.concurrent;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface InterruptionProtocol {

    /**
     * Whether or not the annotated function can be interrupted.
     */
    boolean interruptable() default true;

}
