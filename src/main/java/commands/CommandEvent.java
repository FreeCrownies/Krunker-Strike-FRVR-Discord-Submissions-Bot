package commands;

import core.utils.JDAUtil;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class CommandEvent extends GenericChannelEvent {

    private final Member member;
    private final SlashCommandInteractionEvent slashCommandInteractionEvent;
    private final MessageReceivedEvent messageReceivedEvent;
    private final ButtonInteractionEvent buttonInteractionEvent;
    private final boolean dmCommand;

    public CommandEvent(@NotNull SlashCommandInteractionEvent event) {
        super(event.getJDA(), event.getResponseNumber(), event.getGuildChannel());
        this.slashCommandInteractionEvent = event;
        this.messageReceivedEvent = null;
        this.buttonInteractionEvent = null;
        this.member = event.getMember();
        this.dmCommand = false;
    }

    public CommandEvent(@NotNull MessageReceivedEvent event) {
        super(event.getJDA(), event.getResponseNumber(), event.getChannel());
        this.slashCommandInteractionEvent = null;
        this.messageReceivedEvent = event;
        this.buttonInteractionEvent = null;
        this.member = event.getMember();
        this.dmCommand = event.getChannelType() == ChannelType.PRIVATE;
    }

    public CommandEvent(@NotNull ButtonInteractionEvent event) {
        super(event.getJDA(), event.getResponseNumber(), event.getChannel());
        this.slashCommandInteractionEvent = null;
        this.messageReceivedEvent = null;
        this.buttonInteractionEvent = event;
        this.member = event.getMember();
        this.dmCommand = false;
    }

    @Nullable
    public SlashCommandInteractionEvent getSlashCommandInteractionEvent() {
        return slashCommandInteractionEvent;
    }

    @Nullable
    public MessageReceivedEvent getMessageReceivedEvent() {
        return messageReceivedEvent;
    }

    @Nullable
    public ButtonInteractionEvent getButtonInteractionEvent() {
        return buttonInteractionEvent;
    }

    public boolean isSlashCommandInteractionEvent() {
        return slashCommandInteractionEvent != null;
    }

    public boolean isGuildMessageReceivedEvent() {
        return messageReceivedEvent != null;
    }

    public boolean isButtonInteractionEvent() {
        return buttonInteractionEvent != null;
    }

    public RestAction<Message> replyMessage(String content, Collection<ActionRow> actionRows) {
        if (isGuildMessageReceivedEvent()) {
            return JDAUtil.replyMessage(messageReceivedEvent.getMessage(), content)
                    .setComponents(actionRows);
        } else if (isSlashCommandInteractionEvent()) {
            return slashCommandInteractionEvent.getHook().sendMessage(content)
                    .setComponents(actionRows);
        } else {
            return buttonInteractionEvent.getHook().sendMessage(content)
                    .setComponents(actionRows);
        }
    }

    public RestAction<Message> replyMessageEmbeds(List<MessageEmbed> embeds, Collection<ActionRow> actionRows) {
        if (isGuildMessageReceivedEvent()) {
            return JDAUtil.replyMessageEmbeds(messageReceivedEvent.getMessage(), embeds)
                    .setComponents(actionRows);
        } else if (isSlashCommandInteractionEvent()) {
            return slashCommandInteractionEvent.getHook().sendMessageEmbeds(embeds)
                    .setComponents(actionRows);
        } else {
            return buttonInteractionEvent.getHook().sendMessageEmbeds(embeds)
                    .setComponents(actionRows);
        }
    }

    public GuildMessageChannel getGuildMessageChannel() {
        Channel channel = getChannel();
        if (channel instanceof GuildMessageChannel) {
            return (GuildMessageChannel) channel;
        }
        throw new IllegalStateException("Cannot convert channel of type $channelType to GuildMessageChannel");
    }

    public TextChannel getTextChannel() {
        Channel channel = getChannel();
        if (channel instanceof TextChannel) {
            return (TextChannel) channel;
        }
        throw new IllegalStateException("Cannot convert channel of type $channelType to TextChannel");
    }

    @Nonnull
    public User getUser() {
        return dmCommand ?
                getMessageReceivedEvent().getAuthor() :
                getMember().getUser();
    }

    @Nonnull
    public Member getMember() {
        return member;
    }

    @Nullable
    public Member getRepliedMember() {
        MessageReference messageReference;
        Message messageReferenceMessage;
        Member member;
        if (messageReceivedEvent != null &&
                (messageReference = messageReceivedEvent.getMessage().getMessageReference()) != null &&
                (messageReferenceMessage = messageReference.getMessage()) != null &&
                (member = messageReferenceMessage.getMember()) != null
        ) {
            return member;
        } else {
            return null;
        }
    }

}