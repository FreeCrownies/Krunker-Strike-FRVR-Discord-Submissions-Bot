package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.util.ArrayList;

public abstract class WebhookReceivedAbstract extends DiscordEventAbstract {

    private Instant startTime;

    public static void onWebhookReceivedStatic(MessageReceivedEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        if (!event.isWebhookMessage()) {
            return;
        }

        Instant startTime = Instant.now();
        execute(listenerList, null, event.getGuild().getIdLong(),
                listener -> {
                    ((WebhookReceivedAbstract) listener).setStartTime(startTime);
                    return ((WebhookReceivedAbstract) listener).onWebhookReceived(event);
                }
        );
    }

    public abstract boolean onWebhookReceived(MessageReceivedEvent event) throws Throwable;

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

}
