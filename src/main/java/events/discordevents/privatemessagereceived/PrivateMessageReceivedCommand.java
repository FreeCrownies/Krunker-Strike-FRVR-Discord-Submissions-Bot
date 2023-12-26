package events.discordevents.privatemessagereceived;

import commands.*;
import commands.listeners.MessageInputResponse;
import commands.listeners.OnMessageInputListener;
import constants.Language;
import core.AsyncTimer;
import core.MainLogger;
import core.utils.ExceptionUtil;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.PrivateMessageReceivedAbstract;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@DiscordEvent
public class PrivateMessageReceivedCommand extends PrivateMessageReceivedAbstract {

    @Override
    public boolean onPrivateMessageReceived(MessageReceivedEvent event) throws Throwable {
        String content = event.getMessage().getContentRaw();
        String prefix = "!";
        if (!content.toLowerCase().startsWith(prefix)) {
            return manageMessageInput(event);
        }
        String newContent = content.substring(prefix.length()).trim();
        if (newContent.contains("  ")) newContent = newContent.replace("  ", " ");

        String commandTrigger = newContent.split(" ")[0].toLowerCase();
        if (newContent.contains("<") && newContent.split("<")[0].length() < commandTrigger.length()) {
            commandTrigger = newContent.split("<")[0].toLowerCase();
        }

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

    private boolean manageMessageInput(MessageReceivedEvent event) {
        List<CommandListenerMeta<?>> listeners = CommandContainer.getListeners(OnMessageInputListener.class).stream()
                .filter(listener -> listener.check(event) == CommandListenerMeta.CheckResponse.ACCEPT)
                .sorted((l1, l2) -> l2.getCreationTime().compareTo(l1.getCreationTime()))
                .collect(Collectors.toList());

        if (listeners.size() > 0) {
            try (AsyncTimer timeOutTimer = new AsyncTimer(Duration.ofSeconds(30))) {
                timeOutTimer.setTimeOutListener(t -> {
                    MainLogger.get().error("Message input \"{}\" stuck", event.getMessage().getContentRaw(), ExceptionUtil.generateForStack(t));
                });

                for (CommandListenerMeta<?> listener : listeners) {
                    MessageInputResponse messageInputResponse = ((OnMessageInputListener) listener.getCommand()).processMessageInput(event);
                    if (messageInputResponse != null) {
                        return true;
                    }
                }
                return true;
            } catch (InterruptedException e) {
                MainLogger.get().error("Interrupted exception", e);
            }
        } else {
            return false;
        }
        return false;
    }

}
