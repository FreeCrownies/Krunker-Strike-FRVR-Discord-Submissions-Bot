package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.util.ArrayList;

public abstract class GuildVoiceLeaveAbstract extends DiscordEventAbstract {

    public static void onGuildVoiceLeaveStatic(GuildVoiceUpdateEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getMember().getUser(), event.getGuild().getIdLong(),
                listener -> ((GuildVoiceLeaveAbstract) listener).onGuildVoiceLeave(event)
        );
    }

    public abstract boolean onGuildVoiceLeave(GuildVoiceUpdateEvent event) throws Throwable;

}