package core.utils;

import constants.Emojis;
import constants.Language;
import core.ShardManager;
import core.TextManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public final class StringUtil {

    private StringUtil() {
    }

    public static boolean stringIsDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean stringIsLong(String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean stringIsInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean stringIsIntInRange(String string, int min, int max) {
        if (!stringIsInt(string)) {
            return false;
        } else {
            int i = Integer.parseInt(string);
            return i >= min && i <= max;
        }
    }

    public static boolean stringIsLetters(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    public static long filterLongFromString(String string) {
        StringBuilder numberString = new StringBuilder();

        for (char c : string.replace(",", ".").toCharArray()) {
            if (Character.isDigit(c)) numberString.append(c);
            if (c == '.') break;
        }

        if (numberString.toString().length() == 0) return -1;

        long num = Long.MAX_VALUE;

        try {
            num = Long.parseLong(numberString.toString());
        } catch (Throwable e) {
            //Ignore
        }

        return num;
    }

    public static double filterDoubleFromString(String string) {
        StringBuilder numberString = new StringBuilder();

        for (char c : string.replace(",", ".").toCharArray()) {
            if (Character.isDigit(c)) numberString.append(c);
            if (c == '.') {
                if (numberString.toString().contains(".")) {
                    break;
                } else {
                    numberString.append(".");
                }
            }
        }

        if (numberString.toString().length() == 0) return -1;
        double num = -1;

        try {
            num = Double.parseDouble(numberString.toString());
        } catch (Throwable e) {
            //Ignore
        }

        return num;
    }

    public static String filterDoubleString(String string) {
        StringBuilder numberString = new StringBuilder();
        string = string.replace(",", ".");

        for (char c : string.toCharArray()) {
            if (Character.isDigit(c)) {
                numberString.append(c);
            } else if (c == '.') {
                if (numberString.toString().contains(".")) {
                    break;
                } else {
                    numberString.append(".");
                }
            } else {
                break;
            }
        }

        String filteredString = numberString.toString();
        if (filteredString.endsWith(".")) {
            filteredString = filteredString.substring(0, filteredString.length() - 1);
        }

        return filteredString;
    }

    public static String filterLettersFromString(String string) {
        for (int i = 0; i < 10; i++) {
            string = string.replace(String.valueOf(i), "");
        }
        return string;
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

    public static String[] extractGroups(String string, String start, String end) {
        ArrayList<String> groups = new ArrayList<>();
        while (string.contains(start) && string.contains(end)) {
            int startIndex = string.indexOf(start) + start.length();
            int endIndex = string.indexOf(end, startIndex);
            if (endIndex == -1) break;

            String groupStr = "";
            if (endIndex > startIndex) groupStr = string.substring(startIndex, endIndex);
            groups.add(groupStr);

            string = string.replaceFirst(start, "");
            string = string.replaceFirst(end, "");
        }
        return groups.toArray(new String[0]);
    }

    public static String cutString(String string, String start) {
        string = string.substring(string.indexOf(start) + start.length());
        return string;
    }

    public static String getBar(double value, int number) {
        String[] blocks = {"░", "█"};
        double boxes = value * number;
        StringBuilder sb = new StringBuilder();
        for (double i = 0; i < Math.ceil(boxes); i++) {
            if (i >= boxes) break;
            int index = (int) Math.min(blocks.length - 1, Math.round((boxes - i) * (blocks.length - 1)));
            sb.append(blocks[index]);
        }
        while (sb.length() < number) sb.append(blocks[0]);
        return sb.toString();
    }

    public static String shortenString(String str, int limit) {
        return shortenString(str, limit, "…", false);
    }

    public static String shortenStringLine(String str, int limit) {
        return shortenString(str, limit, "\n…", true);
    }

    public static String shortenString(String str, int limit, String postfix, boolean focusLineBreak) {
        if (str.length() <= limit) {
            return str;
        }

        while (str.length() > limit - postfix.length() && str.contains("\n")) {
            int pos = str.lastIndexOf("\n");
            str = str.substring(0, pos);
        }

        if (!focusLineBreak) {
            while (str.length() > limit - postfix.length() && str.contains(" ")) {
                int pos = str.lastIndexOf(" ");
                str = str.substring(0, pos);
            }
        }

        while (str.length() > 0 && (str.charAt(str.length() - 1) == '.' || str.charAt(str.length() - 1) == ' ' || str.charAt(str.length() - 1) == '\n')) {
            str = str.substring(0, str.length() - 1);
        }

        return str.substring(0, Math.min(str.length(), limit - postfix.length())) + postfix;
    }

    public static String getEmojiForBoolean(GuildMessageChannel channel, boolean bool) {
        if (BotPermissionUtil.can(channel, Permission.MESSAGE_EXT_EMOJI)) {
            return Emojis.SWITCHES_DOT[bool ? 1 : 0];
        } else {
            return bool ? Emojis.CHECK : Emojis.X;
        }
    }

    public static String getOnOffForBoolean(GuildMessageChannel channel, Locale locale, boolean bool) {
        return "**" + getEmojiForBoolean(channel, bool) + " " + TextManager.getString(locale, TextManager.GENERAL, "on_off", bool) + "**";
    }

    public static String solveVariablesOfCommandText(String string, GuildMessageChannel textChannel, Member member, String prefix) {
        return string
                .replace("{#CHANNEL}", textChannel.getAsMention())
                .replace("{MESSAGE_ID}", "824718124225921034")
                .replace("{CHANNEL_ID}", textChannel.getId())
                .replace("{GUILD_ID}", textChannel.getGuild().getId())
                .replace("{@USER}", member.getAsMention())
                .replace("{@BOT}", ShardManager.getSelf().getAsMention())
                .replace("{PREFIX}", prefix);
    }

    public static String doubleToString(double d, int placesAfterPoint) {
        return doubleToString(d, placesAfterPoint, Language.EN.getLocale());
    }

    public static String doubleToString(double d, int placesAfterPoint, Locale locale) {
        StringBuilder pattern = new StringBuilder("#");
        if (placesAfterPoint > 0) pattern.append(".");
        pattern.append("#".repeat(Math.max(0, placesAfterPoint)));

        DecimalFormat df = new DecimalFormat(pattern.toString(), DecimalFormatSymbols.getInstance(Locale.US));
        String str = df.format(d);
        if (Language.from(locale) != Language.EN) {
            str = str.replace(".", ",");
        }

        return str;
    }

    public static boolean stringContainsLetters(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) return true;
        }
        return false;
    }

    public static boolean stringContainsDigits(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) return true;
        }
        return false;
    }

    public static String numToString(float n, Locale locale) {
        return new DecimalFormat("#,###.#", new DecimalFormatSymbols(Language.from(locale) == Language.DE ? Locale.GERMAN : Locale.US)).format(n);
    }

    public static String numToString(long n, Locale locale) {
        return new DecimalFormat("#,###", new DecimalFormatSymbols(Language.from(locale) == Language.DE ? Locale.GERMAN : Locale.US)).format(n);
    }

    public static String numToString(int n, Locale locale) {
        return numToString((long) n, locale);
    }

    public static String numToString(BigInteger n, Locale locale) {
        return new DecimalFormat("#,###.#", new DecimalFormatSymbols(Language.from(locale) == Language.DE ? Locale.GERMAN : Locale.US)).format(n);
    }

    public static String numToHex(long n) {
        return String.format("%x", n).toUpperCase();
    }

    public static String escapeMarkdown(String str) {
        return str.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`")
                .replace("|", "\\|")
                .replace("~", "\\~");
    }

    public static String escapeMarkdownInField(String str) {
        return str.replace("`", "");
    }

    public static double similarityIgnoreLength(String s1, String s2) {
        if (s1.length() > s2.length()) {
            s1 = s1.substring(0, s2.length());
        } else if (s2.length() > s1.length()) s2 = s2.substring(0, s1.length());

        return similarity(s1, s2);
    }

    public static double similarity(String s1, String s2) {
        String longer = s1;
        String shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }
        if (s1.equalsIgnoreCase(s2)) {
            return 1.0;
        }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(
                                    Math.min(newValue, lastValue),
                                    costs[j]
                            ) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    public static boolean stringContainsVague(String str0, String str1) {
        return str0.toLowerCase().replace(" ", "").contains(str1.toLowerCase().replace(" ", ""));
    }

    public static String unescapeHtml(String html) {
        return StringEscapeUtils.unescapeHtml4(html)
                .replaceAll("</?(i|cite)>", "*")
                .replaceAll("</?b>", "**")
                .replaceAll("<[^>]*>", "");
    }

    public static String getRandomString() {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

}