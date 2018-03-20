package com.mxt.anitrend.base.custom.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQuery {

    String value() default "";
}
