package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;

import java.util.ArrayList;

public abstract class GuildMemberUpdatePendingAbstract extends DiscordEventAbstract {

    public abstract boolean onGuildMemberUpdatePending(GuildMemberUpdatePendingEvent event) throws Throwable;

    public static void onGuildMemberUpdatePendingStatic(GuildMemberUpdatePendingEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getUser(), event.getGuild().getIdLong(),
                listener -> ((GuildMemberUpdatePendingAbstract) listener).onGuildMemberUpdatePending(event)
        );
    }

}
