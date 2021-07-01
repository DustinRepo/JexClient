package me.dustin.events.core.annotate;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface EventListener {

    Class<? extends Event>[] events();
    int priority() default 3;

}
