package commands.slashadapters;

import commands.Command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Slash {

    String name() default "";

    Class<? extends Command> command() default Command.class;

    String description() default "";

    boolean guildOnly() default true;

}