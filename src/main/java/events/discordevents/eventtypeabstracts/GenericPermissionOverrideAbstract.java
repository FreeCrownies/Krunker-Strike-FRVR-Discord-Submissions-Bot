package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;

import java.util.ArrayList;

public abstract class GenericPermissionOverrideAbstract extends DiscordEventAbstract {

    public abstract boolean onGenericPermissionOverride(GenericPermissionOverrideEvent event) throws Throwable;

    public static void onGenericPermissionOverrideStatic(GenericPermissionOverrideEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((GenericPermissionOverrideAbstract) listener).onGenericPermissionOverride(event)
        );
    }
}
