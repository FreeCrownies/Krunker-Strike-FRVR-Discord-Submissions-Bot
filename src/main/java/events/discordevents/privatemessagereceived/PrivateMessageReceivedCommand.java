package events.discordevents.privatemessagereceived;

import commands.Command;
import commands.CommandContainer;
import commands.CommandEvent;
import commands.CommandManager;
import constants.Language;
import core.utils.ExceptionUtil;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.PrivateMessageReceivedAbstract;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;

@DiscordEvent
public class PrivateMessageReceivedCommand extends PrivateMessageReceivedAbstract {

    @Override
    public boolean onPrivateMessageReceived(MessageReceivedEvent event) throws Throwable {
        String content = event.getMessage().getContentRaw();
        String prefix = "!";
        if (!content.toLowerCase().startsWith(prefix)) {
            return true;
        }
        String newContent = content.substring(prefix.length()).trim();
        if (newContent.contains("  ")) newContent = newContent.replace("  ", " ");

        String commandTrigger = newContent.split(" ")[0].toLowerCase();
        if (newContent.contains("<") && newContent.split("<")[0].length() < commandTrigger.length()) {
            commandTrigger = newContent.split("<")[0].toLowerCase();
        }
//        commandTrigger = commandTrigger + "_dm";

        String args;
        try {
            args = newContent.substring(commandTrigger.length()).trim();
        } catch (StringIndexOutOfBoundsException e) {
            args = "";
        }

        if (commandTrigger.length() > 0) {
            Class<? extends Command> clazz;
            clazz = CommandContainer.getCommandMap().get(commandTrigger);
            if (clazz != null) {
                Command command = CommandManager.createCommandByClass(clazz, Language.EN.getLocale(), "!");

                try {
                    CommandManager.manage(new CommandEvent(event), command, args, Instant.now());
                } catch (Throwable e) {
                    ExceptionUtil.handleCommandException(e, command);
                }
            }
        }
        return false;
    }

}
