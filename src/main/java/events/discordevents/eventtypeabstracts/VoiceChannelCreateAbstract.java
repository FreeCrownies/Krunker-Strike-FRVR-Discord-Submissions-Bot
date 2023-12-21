package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;

import java.util.ArrayList;

public abstract class VoiceChannelCreateAbstract extends DiscordEventAbstract {

    public static void onVoiceChannelCreateStatic(ChannelCreateEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((VoiceChannelCreateAbstract) listener).onVoiceChannelCreate(event)
        );
    }

    public abstract boolean onVoiceChannelCreate(ChannelCreateEvent event) throws Throwable;

}