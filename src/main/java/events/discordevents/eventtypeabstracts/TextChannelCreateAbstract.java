package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;

import java.util.ArrayList;

public abstract class TextChannelCreateAbstract extends DiscordEventAbstract {

    public static void onTextChannelCreateStatic(ChannelCreateEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((TextChannelCreateAbstract) listener).onTextChannelCreate(event)
        );
    }

    public abstract boolean onTextChannelCreate(ChannelCreateEvent event) throws Throwable;

}