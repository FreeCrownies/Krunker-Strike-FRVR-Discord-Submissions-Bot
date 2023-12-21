package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.interaction.command.ApplicationUpdatePrivilegesEvent;

import java.util.ArrayList;

public abstract class ApplicationUpdatePrivilegesAbstract extends DiscordEventAbstract {

    public static void onApplicationUpdatePrivilegesStatic(ApplicationUpdatePrivilegesEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                listener -> ((ApplicationUpdatePrivilegesAbstract) listener).onApplicationUpdatePrivileges(event)
        );
    }

    public abstract boolean onApplicationUpdatePrivileges(ApplicationUpdatePrivilegesEvent event) throws Throwable;

}
