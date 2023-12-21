package commands.listeners;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandProperties {

    String trigger();

    String[] aliases() default {};

    String emoji();

    boolean executableWithoutArgs() default true;

    boolean deleteOnTimeOut() default false;

    Permission[] botChannelPermissions() default {};

    Permission[] botGuildPermissions() default {};

    Permission[] userChannelPermissions() default {};

    Permission[] userGuildPermissions() default {};

    boolean requiresEmbeds() default true;

    int maxCalculationTimeSec() default 30;

    long[] exclusiveGuilds() default {};

    long[] exclusiveUsers() default {};

    boolean turnOffTimeout() default false;

    int[] releaseDate() default {};

    boolean onlyPublicVersion() default false;

    boolean onlyPremium() default false;

    boolean turnOffLoadingReaction() default false;

    boolean usesExtEmotes() default false;

    boolean requiresFullMemberCache() default false;

}