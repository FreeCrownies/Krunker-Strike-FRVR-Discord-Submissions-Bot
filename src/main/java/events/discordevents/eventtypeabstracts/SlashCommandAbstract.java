package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.ArrayList;

public abstract class SlashCommandAbstract extends DiscordEventAbstract {

    private Instant startTime;

    public static void onSlashCommandStatic(SlashCommandInteractionEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        event.deferReply().queue();
        Instant startTime = Instant.now();
        if (event.getGuild() != null) {
            execute(listenerList, event.getUser(), event.getGuild().getIdLong(),
                    listener -> {
                        ((SlashCommandAbstract) listener).setStartTime(startTime);
                        return ((SlashCommandAbstract) listener).onSlashCommand(event);
                    }
            );
        } else {
            execute(listenerList, event.getUser(),
                    listener -> {
                        ((SlashCommandAbstract) listener).setStartTime(startTime);
                        return ((SlashCommandAbstract) listener).onSlashCommand(event);
                    }
            );
        }
    }

    public abstract boolean onSlashCommand(SlashCommandInteractionEvent event) throws Throwable;

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

}
