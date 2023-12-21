package commands.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface OnStaticReactionAddListener {

    void onStaticReactionAdd(Message message, MessageReactionAddEvent event) throws Throwable;

}