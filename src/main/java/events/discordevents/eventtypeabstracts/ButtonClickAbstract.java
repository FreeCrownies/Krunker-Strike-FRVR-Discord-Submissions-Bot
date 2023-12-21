package events.discordevents.eventtypeabstracts;

import events.discordevents.DiscordEventAbstract;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.ArrayList;

public abstract class ButtonClickAbstract extends DiscordEventAbstract {

    public static void onButtonClickStatic(ButtonInteractionEvent event, ArrayList<DiscordEventAbstract> listenerList) {
        InteractionAbstract.onInteractionStatic(event, listenerList,
                listener -> ((ButtonClickAbstract) listener).onButtonClick(event)
        );
    }

    public abstract boolean onButtonClick(ButtonInteractionEvent event) throws Throwable;

}
