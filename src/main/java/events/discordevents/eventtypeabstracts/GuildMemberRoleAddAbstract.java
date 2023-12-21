package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;

import java.util.ArrayList;

public abstract class GuildMemberRoleAddAbstract extends DiscordEventAbstract {

    public abstract boolean onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) throws Throwable;

    public static void onGuildMemberRoleAddStatic(GuildMemberRoleAddEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getUser(), event.getGuild().getIdLong(),
                listener -> ((GuildMemberRoleAddAbstract) listener).onGuildMemberRoleAdd(event)
        );
    }

}