package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.user.UserTypingEvent;

import java.util.ArrayList;

public abstract class UserTypingAbstract extends DiscordEventAbstract {

    public abstract boolean onUserTyping(UserTypingEvent event) throws Throwable;

    public static void onUserTypingStatic(UserTypingEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        if (event.getGuild() != null) {
            execute(listenerList, event.getUser(), event.getGuild().getIdLong(),
                    listener -> ((UserTypingAbstract) listener).onUserTyping(event)
            );
        }
    }

}