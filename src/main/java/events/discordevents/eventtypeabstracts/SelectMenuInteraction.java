package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.ArrayList;

public abstract class SelectMenuInteraction extends DiscordEventAbstract {

    public static void onSelectMenuStatic(StringSelectInteractionEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        InteractionAbstract.onInteractionStatic(event, listenerList,
                listener -> ((SelectMenuInteraction) listener).onSelectMenu(event)
        );
    }

    public abstract boolean onSelectMenu(StringSelectInteractionEvent event) throws Throwable;

}