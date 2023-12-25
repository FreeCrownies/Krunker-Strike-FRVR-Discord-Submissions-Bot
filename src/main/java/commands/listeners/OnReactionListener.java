package commands.listeners;

import commands.Command;
import commands.CommandContainer;
import commands.CommandListenerMeta;
import core.ExceptionLogger;
import core.MainLogger;
import core.MemberCacheController;
import core.RestActionQueue;
import core.utils.BotPermissionUtil;
import core.utils.EmojiUtil;
import core.utils.ExceptionUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface OnReactionListener extends Drawable {

    boolean onReaction(GenericMessageReactionEvent event) throws Throwable;

    default CompletableFuture<Long> registerReactionListener(User member, Emoji... emojis) {
        Command command = (Command) this;
        return registerReactionListener(member, event -> {
                    boolean ok = event.getUserIdLong() == member.getIdLong() &&
                            event.getMessageIdLong() == ((Command) this).getDrawMessageId().orElse(0L) &&
                            (emojis.length == 0 || Arrays.stream(emojis).anyMatch(emoji -> EmojiUtil.equals(event.getEmoji(), emoji)));
                    return ok ? CommandListenerMeta.CheckResponse.ACCEPT : CommandListenerMeta.CheckResponse.IGNORE;
                }
        ).thenApply(messageId -> {
            command.getGuildMessageChannel().ifPresent(channel -> {
                RestActionQueue restActionQueue = new RestActionQueue();
                Arrays.stream(emojis).forEach(emoji -> restActionQueue.attach(channel.addReactionById(messageId, emoji)));
                if (restActionQueue.isSet()) {
                    restActionQueue.getCurrentRestAction().queue();
                }
            });
            return messageId;
        });
    }

    default CompletableFuture<Long> registerReactionListener(User member, Function<GenericMessageReactionEvent, CommandListenerMeta.CheckResponse> validityChecker) {
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
                onReactionOverridden();
            } catch (Throwable throwable) {
                MainLogger.get().error("Exception on overridden", throwable);
            }
        };

        CommandListenerMeta<GenericMessageReactionEvent> commandListenerMeta =
                new CommandListenerMeta<>(member.getIdLong(), validityChecker, onTimeOut, onOverridden, command);
        CommandContainer.registerListener(OnReactionListener.class, commandListenerMeta);

        try {
            if (command.getDrawMessageId().isEmpty()) {
                EmbedBuilder eb = draw(member);
                if (eb != null) {
                    return command.drawMessage(eb)
                            .thenApply(ISnowflake::getIdLong)
                            .exceptionally(ExceptionLogger.get());
                }
            } else {
                return CompletableFuture.completedFuture(command.getDrawMessageId().get());
            }
        } catch (Throwable e) {
            command.getGuildMessageChannel().ifPresent(channel -> {
                ExceptionUtil.handleCommandException(e, command);
            });
        }

        return CompletableFuture.failedFuture(new NoSuchElementException("No message sent"));
    }

    default void deregisterListenersWithReactionMessage() {
        Command command = (Command) this;
        command.getDrawMessageId().ifPresent(messageId -> {
            command.getGuildMessageChannel().ifPresent(channel -> {
                if (BotPermissionUtil.canReadHistory(channel, Permission.MESSAGE_MANAGE) && command.getCommandEvent().isGuildMessageReceivedEvent()) {
                    Collection<String> messageIds = List.of(String.valueOf(messageId), command.getCommandEvent().getMessageReceivedEvent().getMessageId());
                    channel.deleteMessagesByIds(messageIds).queue();
                } else if (BotPermissionUtil.canReadHistory(channel)) {
                    channel.deleteMessageById(messageId).queue();
                }
            });
        });
        command.deregisterListeners();
    }

    default CompletableFuture<Void> deregisterListenersWithReactions() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Command command = (Command) this;
        command.getDrawMessageId().ifPresentOrElse(messageId -> {
            command.getGuildMessageChannel().ifPresentOrElse(channel -> {
                if (BotPermissionUtil.canReadHistory(channel, Permission.MESSAGE_MANAGE)) {
                    channel.clearReactionsById(messageId)
                            .queue(v -> future.complete(null), future::completeExceptionally);
                } else {
                    future.completeExceptionally(new PermissionException("Missing permissions"));
                }
            }, () -> future.completeExceptionally(new NoSuchElementException("No such text channel")));
        }, () -> future.completeExceptionally(new NoSuchElementException("No such draw message id")));
        command.deregisterListeners();
        return future;
    }

    default void processReaction(GenericMessageReactionEvent event) {
        Command command = (Command) this;

        try {
            if (command.getCommandProperties().requiresFullMemberCache()) {
                MemberCacheController.getInstance().loadMembersFull(event.getGuild()).get();
            } else if (event instanceof MessageReactionRemoveEvent) {
                MemberCacheController.getInstance().loadMember(event.getGuild(), event.getUserIdLong()).get();
            }
            if (event.getUser() == null || event.getUser().isBot()) {
                return;
            }
            if (onReaction(event)) {
                CommandContainer.refreshListeners(command);
                EmbedBuilder eb = draw(event.getUser());
                if (eb != null) {
                    ((Command) this).drawMessage(eb)
                            .exceptionally(ExceptionLogger.get());
                }
            }
        } catch (Throwable e) {
            ExceptionUtil.handleCommandException(e, command);
        }
    }

    default void onReactionOverridden() throws Throwable {
    }

}