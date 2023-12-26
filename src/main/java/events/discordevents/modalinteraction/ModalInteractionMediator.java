package events.discordevents.modalinteraction;

import core.ModalMediator;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.ModalInteractionAbstract;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.function.Consumer;

@DiscordEvent
public class ModalInteractionMediator extends ModalInteractionAbstract {

    @Override
    public boolean onModalInteraction(ModalInteractionEvent event) throws Throwable {
        Consumer<ModalInteractionEvent> consumer = ModalMediator.get(event.getModalId());
        if (consumer != null) {
            consumer.accept(event);
            return false;
        }

        return true;
    }

}