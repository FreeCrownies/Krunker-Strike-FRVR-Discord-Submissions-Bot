package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.util.ArrayList;

public abstract class GuildVoiceMoveAbstract extends DiscordEventAbstract {

    public static void onGuildVoiceMoveStatic(GuildVoiceUpdateEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getMember().getUser(), event.getGuild().getIdLong(),
                listener -> ((GuildVoiceMoveAbstract) listener).onGuildVoiceMove(event)
        );
    }

    public abstract boolean onGuildVoiceMove(GuildVoiceUpdateEvent event) throws Throwable;

}