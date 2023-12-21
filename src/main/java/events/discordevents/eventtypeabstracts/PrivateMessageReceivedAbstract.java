package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public abstract class PrivateMessageReceivedAbstract extends DiscordEventAbstract {

    public static void onPrivateMessageReceivedStatic(MessageReceivedEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        execute(listenerList, event.getAuthor(),
                listener -> ((PrivateMessageReceivedAbstract) listener).onPrivateMessageReceived(event)
        );
    }

    public abstract boolean onPrivateMessageReceived(MessageReceivedEvent event) throws Throwable;

}