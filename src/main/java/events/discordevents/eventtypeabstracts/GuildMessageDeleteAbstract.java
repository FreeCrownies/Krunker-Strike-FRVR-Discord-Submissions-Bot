package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;

import java.util.ArrayList;

public abstract class GuildMessageDeleteAbstract extends DiscordEventAbstract {

    public static void onGuildMessageDeleteStatic(MessageDeleteEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((GuildMessageDeleteAbstract) listener).onGuildMessageDelete(event)
        );
    }

    public abstract boolean onGuildMessageDelete(MessageDeleteEvent event) throws Throwable;

}