package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePrivilegesEvent;

import java.util.ArrayList;

public abstract class ApplicationCommandUpdatePrivilegesAbstract extends DiscordEventAbstract {

    public static void onApplicationCommandUpdatePrivilegesStatic(ApplicationCommandUpdatePrivilegesEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getGuild().getIdLong(),
                (listener) -> ((ApplicationCommandUpdatePrivilegesAbstract) listener).onApplicationCommandUpdatePrivileges(event)
        );
    }

    public abstract boolean onApplicationCommandUpdatePrivileges(ApplicationCommandUpdatePrivilegesEvent event) throws Throwable;

}
