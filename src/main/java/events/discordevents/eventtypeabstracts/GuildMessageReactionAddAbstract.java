package events.discordevents.eventtypeabstracts;

import core.AsyncTimer;
import core.MainLogger;
import core.utils.ExceptionUtil;
import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.time.Duration;
import java.util.ArrayList;

public abstract class GuildMessageReactionAddAbstract extends DiscordEventAbstract {

    public static void onGuildMessageReactionAddStatic(MessageReactionAddEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        try (AsyncTimer timeOutTimer = new AsyncTimer(Duration.ofSeconds(30))) {
            timeOutTimer.setTimeOutListener(t -> {
                MainLogger.get().error("Reaction add \"{}\" of guild {} stuck", event.getEmoji().getAsReactionCode(), event.getGuild().getIdLong(), ExceptionUtil.generateForStack(t));
            });

            execute(listenerList, event.getUser(), event.getGuild().getIdLong(),
                    listener -> ((GuildMessageReactionAddAbstract) listener).onGuildMessageReactionAdd(event)
            );
        } catch (InterruptedException e) {
            MainLogger.get().error("Interrupted exception", e);
        }
    }

    public abstract boolean onGuildMessageReactionAdd(MessageReactionAddEvent event) throws Throwable;

}