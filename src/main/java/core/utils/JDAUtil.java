package core.utils;

import commands.Command;
import core.MemberCacheController;
import core.ShardManager;
import core.components.ActionRows;
import mysql.modules.userprivatechannels.DBUserPrivateChannels;
import mysql.modules.userprivatechannels.PrivateChannelData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class JDAUtil {

    public static Optional<TextChannel> getFirstWritableChannelOfGuild(Guild guild) {
        if (guild.getSystemChannel() != null && BotPermissionUtil.canWriteEmbed(guild.getSystemChannel())) {
            return Optional.of(guild.getSystemChannel());
        } else {
            for (TextChannel channel : guild.getTextChannels()) {
                if (BotPermissionUtil.canWriteEmbed(channel)) {
                    return Optional.of(channel);
                }
            }
        }

        return Optional.empty();
    }

    public static String resolveMentions(Guild guild, String content) {
        for (Member member : MemberCacheController.getInstance().loadMembersFull(guild).join()) {
            content = content.replace(MentionUtil.getUserAsMention(member.getIdLong(), true), "@" + member.getEffectiveName())
                    .replace(MentionUtil.getUserAsMention(member.getIdLong(), false), "@" + member.getEffectiveName());
        }
        for (GuildChannel channel : guild.getChannels()) {
            content = content.replace(channel.getAsMention(), "#" + channel.getName());
        }
        for (Role role : guild.getRoles()) {
            content = content.replace(role.getAsMention(), "@" + role.getName());
        }
        for (Emoji emote : guild.getEmojis()) {
            content = content.replace(emote.getFormatted(), ":" + emote.getName() + ":");
        }
        return content;
    }

    @CheckReturnValue
    public static RestAction<PrivateChannel> openPrivateChannel(Member member) {
        return openPrivateChannel(member.getJDA(), member.getIdLong());
    }

    @CheckReturnValue
    public static RestAction<PrivateChannel> openPrivateChannel(User user) {
        return openPrivateChannel(user.getJDA(), user.getIdLong());
    }

    @CheckReturnValue
    public static RestAction<PrivateChannel> openPrivateChannel(JDA jda, long userId) {
        PrivateChannelData privateChannelData = DBUserPrivateChannels.getInstance().retrieve().get(userId);
        if (privateChannelData != null) {
            PrivateChannel messageChannel = generatePrivateChannel(privateChannelData.getPrivateChannelId());
            return new CompletedRestAction<>(jda, messageChannel, null);
        } else {
            return jda.openPrivateChannelById(userId)
                    .map(privateChannel -> {
                        PrivateChannelData newPrivateChannelData = new PrivateChannelData(userId, privateChannel.getIdLong());
                        DBUserPrivateChannels.getInstance().retrieve().put(userId, newPrivateChannelData);
                        return privateChannel;
                    });
        }
    }

    private static PrivateChannel generatePrivateChannel(long privateChannelId) {
        return new PrivateChannel() {

            @Override
            public long getIdLong() {
                return privateChannelId;
            }

            @Override
            public long getLatestMessageIdLong() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean canTalk() {
                return false;
            }

            @Nullable
            @Override
            public User getUser() {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public RestAction<User> retrieveUser() {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public String getName() {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public ChannelType getType() {
                return ChannelType.PRIVATE;
            }

            @NotNull
            @Override
            public JDA getJDA() {
                return ShardManager.getAnyJDA().get();
            }

            @NotNull
            @Override
            public RestAction<Void> delete() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateMessage(Member member, String content) {
        return sendPrivateMessage(member.getIdLong(), content);
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateMessage(User user, String content) {
        return sendPrivateMessage(user.getIdLong(), content);
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateMessage(long userId, String content) {
        return ShardManager.getAnyJDA().get().openPrivateChannelById(userId).flatMap(
                channel -> channel.sendMessage(content)
        );
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateMessage(Member member, MessageEmbed eb, Button... buttons) {
        return sendPrivateMessage(member.getIdLong(), eb, buttons);
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateMessage(User user, MessageEmbed eb, Button... buttons) {
        return sendPrivateMessage(user.getIdLong(), eb, buttons);
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateMessage(long userId, MessageEmbed eb, Button... buttons) {
        return ShardManager.getAnyJDA().get().openPrivateChannelById(userId).flatMap(
                channel -> channel.sendMessageEmbeds(eb)
                        .setComponents(ActionRows.of(buttons))
        );
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateMessage(long userId, Function<? super PrivateChannel, ? extends RestAction<Message>> flatMap) {
        return ShardManager.getAnyJDA().get().openPrivateChannelById(userId).flatMap(flatMap);
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateFile(long userId, InputStream inputStream, String filename) {
        return ShardManager.getAnyJDA().get().openPrivateChannelById(userId).flatMap(
                channel -> channel.sendFiles(FileUpload.fromData(inputStream, filename))
        );
    }

    @CheckReturnValue
    public static RestAction<Message> sendPrivateFile(long userId, File file) {
        return ShardManager.getAnyJDA().get().openPrivateChannelById(userId).flatMap(
                channel -> channel.sendFiles(FileUpload.fromData(file))
        );
    }

    public static MessageCreateAction replyMessage(Message originalMessage, String content) {
        MessageCreateAction messageAction = originalMessage.getChannel().sendMessage(content);
        messageAction = messageActionSetMessageReference(false, messageAction, originalMessage);
        return messageAction;
    }

    public static MessageCreateAction replyMessageEmbeds(boolean privateMessage, Message originalMessage, List<MessageEmbed> embeds) {
        MessageCreateAction messageAction = originalMessage.getChannel().sendMessageEmbeds(embeds);
        messageAction = messageActionSetMessageReference(privateMessage, messageAction, originalMessage);
        return messageAction;
    }

    public static MessageCreateAction replyMessageEmbeds(Message originalMessage, MessageEmbed embed, MessageEmbed... other) {
        MessageCreateAction messageAction = originalMessage.getChannel().sendMessageEmbeds(embed, other);
        messageAction = messageActionSetMessageReference(false, messageAction, originalMessage);
        return messageAction;
    }

    public static MessageCreateAction messageActionSetMessageReference(boolean privateMessage, MessageCreateAction messageAction, Message originalMessage) {
        if (privateMessage) {
            messageAction = messageAction.setMessageReference(originalMessage.getIdLong());
            return messageAction;
        }
        return messageActionSetMessageReference(messageAction, originalMessage.getGuildChannel(), originalMessage.getIdLong());
    }

    public static MessageCreateAction messageActionSetMessageReference(MessageCreateAction messageAction, GuildMessageChannelUnion textChannel, long messageId) {
        if (BotPermissionUtil.can(textChannel, Permission.MESSAGE_HISTORY)) {
            messageAction = messageAction.setMessageReference(messageId);
        }
        return messageAction;
    }

    public static void deleteCommandMessage(Command command) {
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
    }

}