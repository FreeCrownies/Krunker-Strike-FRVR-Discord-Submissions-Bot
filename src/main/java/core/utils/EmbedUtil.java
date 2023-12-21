package core.utils;

import commands.Command;
import constants.Emojis;
import constants.LogStatus;
import core.TextManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.Locale;
import java.util.Optional;

public class EmbedUtil {

    public static EmbedBuilder setMemberAuthor(EmbedBuilder eb, Member member) {
        return setMemberAuthor(eb, member.getEffectiveName(), member.getUser().getEffectiveAvatarUrl());
    }

    public static EmbedBuilder setMemberAuthor(EmbedBuilder eb, String memberName, String memberAvatarUrl) {
        return eb.setAuthor(memberName, null, memberAvatarUrl);
    }

    public static EmbedBuilder addNoResultsLog(EmbedBuilder eb, Locale locale, String searchString) {
        return addLog(eb, LogStatus.FAILURE, TextManager.getNoResultsString(locale, searchString));
    }

    public static EmbedBuilder addLog(EmbedBuilder eb, String log) {
        return addLog(eb, null, log);
    }

    public static EmbedBuilder addLog(EmbedBuilder eb, LogStatus logStatus, String log) {
        if (log != null && log.length() > 0) {
            String add = "";
            if (logStatus != null) {
                add = switch (logStatus) {
                    case FAILURE -> "❌ ";
                    case SUCCESS -> "✅ ";
                    case WIN -> "🎉 ";
                    case LOSE -> "☠️ ";
                    case WARNING -> "⚠️️ ";
                    case TIME -> "⏲️ ";
                };
            }
            eb.addField(Emojis.ZERO_WIDTH_SPACE, "`" + add + log + "`", false);
        }

        return eb;
    }

    public static EmbedBuilder setFooter(EmbedBuilder eb, Command command) {
        Optional<String> userTagOpt = command.getMemberAsTag();
        if (userTagOpt.isPresent()) {
            eb.setFooter(userTagOpt.get());
        } else {
            eb.setFooter(null);
        }

        return eb;
    }

    public static EmbedBuilder setFooter(EmbedBuilder eb, Command command, String footer) {
        if (footer == null || footer.isEmpty()) {
            return setFooter(eb, command);
        }

        Optional<String> userTagOpt = command.getMemberAsTag();
        if (userTagOpt.isPresent()) {
            eb = eb.setFooter(userTagOpt.get() + " • " + footer);
        } else {
            eb.setFooter(footer);
        }
        return eb;
    }

    public static EmbedBuilder setFooter(EmbedBuilder eb, User user) {
        return eb.setFooter(user.getAsTag());
    }

}