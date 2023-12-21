package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.util.ArrayList;

public abstract class GuildVoiceJoinAbstract extends DiscordEventAbstract {

    public static void onGuildVoiceJoinStatic(GuildVoiceUpdateEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getMember().getUser(), event.getGuild().getIdLong(),
                listener -> ((GuildVoiceJoinAbstract) listener).onGuildVoiceJoin(event)
        );
    }

    public abstract boolean onGuildVoiceJoin(GuildVoiceUpdateEvent event) throws Throwable;

}