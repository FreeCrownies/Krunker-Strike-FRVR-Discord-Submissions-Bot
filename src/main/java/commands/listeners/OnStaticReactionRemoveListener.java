package commands.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

public interface OnStaticReactionRemoveListener {

    void onStaticReactionRemove(Message message, MessageReactionRemoveEvent event) throws Throwable;

}