package me.dustin.jex.command.core.annotate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/*
 * @Author Dustin
 * 9/29/2019
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Cmd {

    String name();

    String syntax();

    String description();

    String[] alias() default {};

}
