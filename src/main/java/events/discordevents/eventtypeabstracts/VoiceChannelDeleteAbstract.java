package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;

import java.util.ArrayList;

public abstract class VoiceChannelDeleteAbstract extends DiscordEventAbstract {

    public static void onVoiceChannelDeleteStatic(ChannelDeleteEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((VoiceChannelDeleteAbstract) listener).onVoiceChannelDelete(event)
        );
    }

    public abstract boolean onVoiceChannelDelete(ChannelDeleteEvent event) throws Throwable;

}