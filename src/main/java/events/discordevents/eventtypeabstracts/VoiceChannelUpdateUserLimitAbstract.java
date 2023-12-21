package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateUserLimitEvent;

import java.util.ArrayList;

public abstract class VoiceChannelUpdateUserLimitAbstract extends DiscordEventAbstract {

    public static void onVoiceChannelUpdateUserLimitStatic(ChannelUpdateUserLimitEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((VoiceChannelUpdateUserLimitAbstract) listener).onVoiceChannelUpdateUserLimit(event)
        );
    }

    public abstract boolean onVoiceChannelUpdateUserLimit(ChannelUpdateUserLimitEvent event) throws Throwable;

}