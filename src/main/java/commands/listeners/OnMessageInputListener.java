package commands.listeners;

import commands.Command;
import commands.CommandContainer;
import commands.CommandListenerMeta;
import core.ExceptionLogger;
import core.MainLogger;
import core.MemberCacheController;
import core.utils.BotPermissionUtil;
import core.utils.ExceptionUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public interface OnMessageInputListener extends Drawable {

    MessageInputResponse onMessageInput(MessageReceivedEvent event, String input) throws Throwable;

    default void registerMessageInputListener(Member member) {
        registerMessageInputListener(member, true);
    }

    default void registerMessageInputListener(Member member, boolean draw) {
        Command command = (Command) this;
        registerMessageInputListener(member, draw, event -> {
                    boolean ok = event.getMember().getIdLong() == member.getIdLong() &&
                            event.getChannel().getIdLong() == command.getTextChannelId().orElse(0L);
                    return ok ? CommandListenerMeta.CheckResponse.ACCEPT : CommandListenerMeta.CheckResponse.IGNORE;
                }
        );
    }

    default void registerMessageInputListener(Member member, boolean draw, Function<MessageReceivedEvent, CommandListenerMeta.CheckResponse> validityChecker) {
        Command command = (Command) this;

        Runnable onTimeOut = () -> {
            try {
                command.deregisterListeners();
                // DELETE COMMAND MESSAGE
                command.onListenerTimeOutSuper();
            } catch (Throwable throwable) {
                MainLogger.get().error("Exception on time out", throwable);
            }
        };

        Runnable onOverridden = () -> {
            try {
                onMessageInputOverridden();
            } catch (Throwable throwable) {
                MainLogger.get().error("Exception on overridden", throwable);
            }
        };

        CommandListenerMeta<MessageReceivedEvent> commandListenerMeta =
                new CommandListenerMeta<>(member.getIdLong(), validityChecker, onTimeOut, onOverridden, command);
        CommandContainer.registerListener(OnMessageInputListener.class, commandListenerMeta);

        try {
            if (draw && command.getDrawMessageId().isEmpty()) {
                EmbedBuilder eb = draw(member);
                if (eb != null) {
                    command.drawMessage(eb)
                            .exceptionally(ExceptionLogger.get());
                }
            }
        } catch (Throwable e) {
            command.getGuildMessageChannel().ifPresent(channel -> {
                ExceptionUtil.handleCommandException(e, command);
            });
        }
    }

    default MessageInputResponse processMessageInput(MessageReceivedEvent event) {
        Command command = (Command) this;
        AtomicBoolean isProcessing = new AtomicBoolean(true);

        command.addLoadingReaction(event.getMessage(), isProcessing);
        try {
            if (command.getCommandProperties().requiresFullMemberCache()) {
                MemberCacheController.getInstance().loadMembersFull(event.getGuild()).get();
            }
            MessageInputResponse messageInputResponse = onMessageInput(event, event.getMessage().getContentRaw());
            if (messageInputResponse != null) {
                if (messageInputResponse == MessageInputResponse.SUCCESS) {
                    CommandContainer.refreshListeners(command);
                    if (BotPermissionUtil.can(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
                        event.getMessage().delete().queue();
                    }
                }

                EmbedBuilder eb = draw(event.getMember());
                if (eb != null) {
                    ((Command) this).drawMessage(eb)
                            .exceptionally(ExceptionLogger.get());
                }
            }
            return messageInputResponse;
        } catch (Throwable e) {
            ExceptionUtil.handleCommandException(e, command);
            return MessageInputResponse.ERROR;
        } finally {
            isProcessing.set(false);
        }
    }

    default void onMessageInputOverridden() throws Throwable {
    }

}