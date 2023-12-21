package core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import commands.Category;
import constants.Emojis;
import constants.Language;
import constants.RegexPatterns;
import core.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextManager {

    public static final String COMMANDS = "commands";
    public static final String GENERAL = "general";
    public static final String PERMISSIONS = "permissions";
    public static final String VERSIONS = "versions";

    private static final LoadingCache<Pair<String, Locale>, ResourceBundle> bundles = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public ResourceBundle load(@NotNull Pair<String, Locale> pair) {
                    return ResourceBundle.getBundle(pair.getLeft(), pair.getRight(), new UTF8Control());
                }
            });

    public static String getString(Locale locale, Category category, String key, boolean secondOption, String... args) {
        return getString(locale, category.getId(), key, secondOption, args);
    }

    public static String getString(Locale locale, String category, String key, boolean secondOption, String... args) {
        if (!secondOption) {
            return getString(locale, category, key, 0, args);
        } else {
            return getString(locale, category, key, 1, args);
        }
    }

    public static String getString(Locale locale, Category category, String key, String... args) {
        return getString(locale, category.getId(), key, args);
    }

    public static String getString(Locale locale, String category, String key, String... args) {
        return getString(locale, category, key, -1, args);
    }

    public static String getString(Locale locale, Category category, String key, int option, String... args) {
        return getString(locale, category.getId(), key, option, args);
    }

    public static String getString(Locale locale, String category, String key, int option, String... args) {
        ResourceBundle texts;
        try {
            texts = bundles.get(new Pair<>(category, new Locale(locale.toString().toLowerCase())));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (texts.containsKey(key)) {
            try {
                String text = texts.getString(key);
                String[] placeholders = extractGroups(RegexPatterns.TEXT_PLACEHOLDER_PATTERN, text);
                text = processMultiOptions(text, option);
                text = processReferences(text, placeholders, category, locale);
                text = processParams(text, placeholders, args);
                text = processEmojis(text, placeholders);
                text = processRandom(text);

                return text;
            } catch (Throwable e) {
                MainLogger.get().error("Text error for key {} in {} with locale {}", key, category, locale, e);
            }
        } else {
            MainLogger.get().error("Key {} not found in {} with locale {}", key, category, locale);
        }

        return "???";
    }

    private static String[] extractGroups(Pattern pattern, String text) {
        ArrayList<String> placeholderList = new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while(m.find()) {
            placeholderList.add(m.group("inner"));
        }
        return placeholderList.toArray(new String[0]);
    }

    private static String processEmojis(String text, String[] placeholders) {
        List<Pair<String, String>> emojiPairs = List.of(
                new Pair<>("INCOME", Emojis.INCOME),
                new Pair<>("COINS", Emojis.COINS),
                new Pair<>("MONEY", Emojis.COINS),
                new Pair<>("DAILY_STREAK", Emojis.DAILY_STREAK),
                new Pair<>("STONES", Emojis.STONES),
                new Pair<>("IRON", Emojis.IRON),
                new Pair<>("GOLD", Emojis.GOLD)
        );

        for (String placeholder : placeholders) {
            for (Pair<String, String> emojiPair : emojiPairs) {
                if (emojiPair.getLeft().equals(placeholder)) {
                    text = text.replace("{" + emojiPair.getLeft() + "}", emojiPair.getRight());
                }
            }
        }

        return text;
    }

    private static String processParams(String text, String[] placeholders, String[] args) {
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", args[i]);
        }

        return text;
    }

    private static String processMultiOptions(String text, int option) {
        String[] groups = extractGroups(RegexPatterns.TEXT_MULTI_OPTION_PATTERN, text);

        for (String group : groups) {
            if (group.contains("|")) {
                text = text.replace("[" + group + "]", group.split("\\|")[option]);
            }
        }

        return text.replace("\\[", "[").replace("\\]", "]");
    }

    private static String processReferences(String text, String[] placeholders, String category, Locale locale) {
        for (String placeholder : placeholders) {
            if (placeholder.contains(".")) {
                String[] parts = placeholder.split("\\.");
                if (parts[0].equals("this")) {
                    parts[0] = category;
                }
                String newValue = getString(locale, parts[0], parts[1]);
                text = text.replace("{" + placeholder + "}", newValue);
            }
        }

        return text;
    }

    private static String processRandom(String text) {
        if (text.startsWith("%random%")) {
            String[] options = text.replaceFirst("%random%", "").split("\n");
            text = options[new Random().nextInt(options.length)];
        }
        return text;
    }

    public static String getNoResultsString(Locale locale, String content) {
        return TextManager.getString(locale, TextManager.GENERAL, "no_results_description", StringUtil.shortenString(content, 32));
    }

    public static int getKeySize(String category) {
        ResourceBundle texts = ResourceBundle.getBundle(category, Language.EN.getLocale());
        return texts.keySet().size();
    }


    public static String getStatusString (boolean b) {
        return b ? "**✅ AN**" : "**❌ AUS**";
    }
}
