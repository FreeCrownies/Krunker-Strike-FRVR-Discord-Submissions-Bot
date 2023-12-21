package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

import java.util.ArrayList;

public abstract class TextChannelDeleteAbstract extends DiscordEventAbstract {

    public static void onTextChannelDeleteStatic(ChannelDeleteEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((TextChannelDeleteAbstract) listener).onTextChannelDelete(event)
        );
    }

    public abstract boolean onTextChannelDelete(ChannelDeleteEvent event) throws Throwable;

}