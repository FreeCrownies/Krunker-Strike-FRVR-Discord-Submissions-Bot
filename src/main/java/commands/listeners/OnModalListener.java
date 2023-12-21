package commands.listeners;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public interface OnModalListener {

    boolean onModal(ModalInteractionEvent event);


}
