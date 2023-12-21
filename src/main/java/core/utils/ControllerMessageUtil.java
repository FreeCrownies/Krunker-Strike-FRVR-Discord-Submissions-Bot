package core.utils;

import commands.listeners.MessageInputResponse;
import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import core.DiscordFile;
import core.MainLogger;
import core.TextManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ControllerMessageUtil {

    public static MessageInputResponse updateImage(MessageReceivedEvent event, NavigationAbstract command, int STATE_BACK, ValueConsumer<String> urlConsumer) {
        return updateImage(event, command, STATE_BACK, urlConsumer,  command.getString("set_image"));
    }

    public static MessageInputResponse updateImage(MessageReceivedEvent event, NavigationAbstract command, int STATE_BACK, ValueConsumer<String> urlConsumer, String logSuccess) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        DiscordFile discordFile;
        if (!attachments.isEmpty()) {
            Message.Attachment attachment = attachments.get(0);
            discordFile = new DiscordFile(attachment);
        } else {
            List<URL> urls = MentionUtil.getImages(event.getMessage().getContentRaw()).getList();
            if (urls.isEmpty()) {
                command.setLog(LogStatus.FAILURE, TextManager.getNoResultsString(command.getLocale(), event.getMessage().getContentRaw()));
                return MessageInputResponse.FAILED;
            } else {
                try {
                    discordFile = new DiscordFile(urls.get(0));
                } catch (IOException e) {
                    MainLogger.get().error("Error while downloading file", e);
                    return MessageInputResponse.FAILED;
                }
            }
        }

        boolean success = discordFile.upload();
        if (success) {
            try {
                urlConsumer.accept(discordFile.getUrl());
            } catch (Exception e) {
                MainLogger.get().error("Error while changing image in command {}", command.getTrigger(), e);
            }
            command.setLog(LogStatus.SUCCESS, logSuccess);
            command.setState(STATE_BACK);
            return MessageInputResponse.SUCCESS;
        }

        command.setLog(LogStatus.FAILURE, TextManager.getNoResultsString(command.getLocale(), event.getMessage().getContentRaw()));
        return MessageInputResponse.FAILED;
    }

    public static MessageInputResponse updateColor(String input, NavigationAbstract command, int STATE_BACK, ValueConsumer<Color> colorConsumer) {
        return updateColor(input, command, STATE_BACK, colorConsumer, command.getString("set_color"));
    }

    public static MessageInputResponse updateColor(String input, NavigationAbstract command, int STATE_BACK, ValueConsumer<Color> colorConsumer, String logSuccess) {
        if (!input.isEmpty()) {
            if (!input.startsWith("#")) input = "#" + input;
            if (ColorUtil.isColorCode(input)) {
                try {
                    colorConsumer.accept(ColorUtil.getColor(input).get());
                } catch (Exception e) {
                    MainLogger.get().error("Error while changing color in command {}", command.getTrigger(), e);
                }
                command.setLog(LogStatus.SUCCESS, logSuccess);
                command.setState(STATE_BACK);
                return MessageInputResponse.SUCCESS;
            } else {
                command.setLog(LogStatus.FAILURE, TextManager.getString(command.getLocale(), TextManager.GENERAL, "invalid_color", input));
                return MessageInputResponse.FAILED;
            }
        }
        return null;
    }

    public static MessageInputResponse updateTextChannel(MessageReceivedEvent event, String input, NavigationAbstract command, Integer STATE_BACK, ValueConsumer<TextChannel> textChannelConsumer) {
        List<TextChannel> channelList = MentionUtil.getTextChannels(event.getGuild(), input).getList();
        if (!channelList.isEmpty()) {
            TextChannel channel = channelList.get(0);
            if (command.checkWriteInChannelWithLog(channel)) {
                textChannelConsumer.accept(channel);
                command.setLog(LogStatus.SUCCESS, command.getString("set_channel"));
                if (STATE_BACK != null) command.setState(STATE_BACK);
                return MessageInputResponse.SUCCESS;
            } else {
                return MessageInputResponse.FAILED;
            }

        }
        command.setLog(LogStatus.FAILURE, TextManager.getNoResultsString(command.getLocale(), input));
        return MessageInputResponse.FAILED;
    }

    public static MessageInputResponse updateStandardGuildMessageChannel(MessageReceivedEvent event, String input, NavigationAbstract command, Integer STATE_BACK, ValueConsumer<StandardGuildMessageChannel> standardGuildMessageChannelValueConsumer) {
        List<StandardGuildMessageChannel> channelList = MentionUtil.getStandardGuildMessageChannels(event.getGuild(), input).getList();
        if (!channelList.isEmpty()) {
            StandardGuildMessageChannel channel = channelList.get(0);
            if (command.checkWriteInChannelWithLog(channel)) {
                standardGuildMessageChannelValueConsumer.accept(channel);
                command.setLog(LogStatus.SUCCESS, command.getString("set_channel"));
                if (STATE_BACK != null) command.setState(STATE_BACK);
                return MessageInputResponse.SUCCESS;
            } else {
                return MessageInputResponse.FAILED;
            }

        }
        command.setLog(LogStatus.FAILURE, TextManager.getNoResultsString(command.getLocale(), input));
        return MessageInputResponse.FAILED;
    }

    public static MessageInputResponse updateGuildMessageChannel(MessageReceivedEvent event, String input, NavigationAbstract command, Integer STATE_BACK, ValueConsumer<GuildMessageChannel> guildMessageChannelValueConsumer) {
        List<GuildMessageChannel> channelList = MentionUtil.getGuildMessageChannels(event.getGuild(), input).getList();
        if (!channelList.isEmpty()) {
            GuildMessageChannel channel = channelList.get(0);
            if (command.checkWriteInChannelWithLog(channel)) {
                guildMessageChannelValueConsumer.accept(channel);
                command.setLog(LogStatus.SUCCESS, command.getString("set_channel"));
                if (STATE_BACK != null) command.setState(STATE_BACK);
                return MessageInputResponse.SUCCESS;
            } else {
                return MessageInputResponse.FAILED;
            }

        }
        command.setLog(LogStatus.FAILURE, TextManager.getNoResultsString(command.getLocale(), input));
        return MessageInputResponse.FAILED;
    }

    public interface ValueConsumer<T> {
        void accept(T t);
    }

}