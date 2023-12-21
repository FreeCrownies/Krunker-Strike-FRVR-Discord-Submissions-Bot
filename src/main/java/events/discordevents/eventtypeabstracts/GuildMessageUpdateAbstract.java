package events.discordevents.eventtypeabstracts;

import core.cache.MessageCache;
import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.util.ArrayList;

public abstract class GuildMessageUpdateAbstract extends DiscordEventAbstract {

    public static void onGuildMessageUpdateStatic(MessageUpdateEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        MessageCache.put(event.getMessage());

        Member member = event.getMember();
        if (member == null) {
            return;
        }

        execute(listenerList, member.getUser(), event.getGuild().getIdLong(),
                listener -> ((GuildMessageUpdateAbstract) listener).onGuildMessageUpdate(event)
        );
    }

    public abstract boolean onGuildMessageUpdate(MessageUpdateEvent event) throws Throwable;

}