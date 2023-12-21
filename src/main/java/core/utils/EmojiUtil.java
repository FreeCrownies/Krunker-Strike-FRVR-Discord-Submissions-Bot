package core.utils;

import constants.Emojis;
import constants.RegexPatterns;
import core.ShardManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Optional;
import java.util.regex.Matcher;

public class EmojiUtil {

    public static boolean emojiIsUnicode(String emoji) {
        return !emoji.contains(":");
    }

    public static long extractIdFromEmoteMention(String emoji) {
        Matcher m = RegexPatterns.EMOTE.matcher(emoji);
        if (m.find()) {
            return Long.parseLong(m.group("id"));
        }
        return 0L;
    }

    public static String emojiAsReactionTag(String mention) {
        return mention.replace("<a:", "")
                .replace("<:", "")
                .replace(">", "");
    }

    public static boolean equals(Emoji emoji, Emoji otherEmoji) {
        if (emoji instanceof CustomEmoji && otherEmoji instanceof CustomEmoji) {
            return ((CustomEmoji) emoji).getIdLong() == ((CustomEmoji) otherEmoji).getIdLong();
        } else {
            return emoji.getFormatted().equals(otherEmoji.getFormatted());
        }
    }

    public static Optional<MessageReaction> getMessageReactionFromMessage(Message message, String emoji) {
        return message.getReactions().stream()
                .filter(r -> r.getEmoji().getAsReactionCode().equals(emoji))
                .findFirst();
    }

    public static String getLoadingEmojiMention(GuildMessageChannel channel) {
        if (channel != null && BotPermissionUtil.canReadHistory(channel, Permission.MESSAGE_EXT_EMOJI)) {
            return Emojis.LOADING;
        } else {
            return "⏳";
        }
    }

    public static String getLoadingEmojiTag(GuildMessageChannelUnion channel) {
        if (channel != null && BotPermissionUtil.canReadHistory(channel, Permission.MESSAGE_EXT_EMOJI)) {
            return EmojiUtil.emojiAsReactionTag(Emojis.LOADING);
        } else {
            return "⏳";
        }
    }

    public static Emoji toEmoji(String emojiTag) {
        if (emojiIsUnicode(emojiTag)) {
            return Emoji.fromUnicode(emojiTag);
        }

        return ShardManager.getLocalEmoteById(extractIdFromEmoteMention(emojiTag))
                .orElse(null);
    }

}