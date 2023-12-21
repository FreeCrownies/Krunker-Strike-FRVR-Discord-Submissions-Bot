package commands.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface OnStaticButtonListener {

    void onStaticButton(ButtonInteractionEvent event) throws Throwable;

}