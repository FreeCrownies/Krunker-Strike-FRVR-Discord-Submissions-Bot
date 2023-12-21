package core.emoji;

import constants.Emojis;
import core.GlobalThreadPool;
import core.MainLogger;
import emoji4j.Emoji;
import emoji4j.EmojiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmojiTable {

    private static final ArrayList<String> emojis = new ArrayList<>();

    public static void load() {
//      if (Program.productionMode()) {
        GlobalThreadPool.getExecutorService().submit(() -> {
            emojis.addAll(List.of(Emojis.LETTERS));
            try {
                EmojiUnicodePointAndValueMaker emojiUnicodePointAndValueMaker = new EmojiUnicodePointAndValueMaker();

                MainLogger.get().info("Downloading emoji lists...");
                emojiUnicodePointAndValueMaker.build("https://unicode.org/emoji/charts/full-emoji-modifiers.html")
                        .forEach(emoji -> emojis.add(emoji.toEmoji()));

                emojiUnicodePointAndValueMaker.build("https://unicode.org/emoji/charts/full-emoji-list.html")
                        .forEach(emoji -> emojis.add(emoji.toEmoji()));

                MainLogger.get().info("Emoji lists completed with {} emojis", emojis.size());
            } catch (Throwable e) {
                MainLogger.get().error("Exception on emoji load", e);
                loadFromArchive();
            }
        });
//      }
    }

    public static void loadFromArchive() {
        try {
            EmojiUnicodePointAndValueMaker emojiUnicodePointAndValueMaker = new EmojiUnicodePointAndValueMaker();

            MainLogger.get().info("Downloading emoji lists from cache...");
            emojiUnicodePointAndValueMaker.build("https://web.archive.org/web/20210515172234/https://unicode.org/emoji/charts/full-emoji-modifiers.html")
                    .forEach(emoji -> emojis.add(emoji.toEmoji()));

            emojiUnicodePointAndValueMaker.build("https://web.archive.org/web/20210616052941/https://unicode.org/emoji/charts/full-emoji-list.html")
                    .forEach(emoji -> emojis.add(emoji.toEmoji()));

            emojis.addAll(List.of(Emojis.LETTERS));
            MainLogger.get().info("Emoji lists completed with {} emojis", emojis.size());
        } catch (Throwable e) {
            MainLogger.get().error("Exception on emoji cache load", e);
            loadFromLibrary();
        }
    }

    public static void loadFromLibrary() {
        for (Emoji emoji : EmojiManager.data()) {
            emojis.add(emoji.getEmoji());
        }
        MainLogger.get().info("Emoji lists completed with {} emojis", emojis.size());
    }

    public static Optional<String> extractFirstEmoji(String input) {
        Optional<String> emojiResult = Optional.empty();
        int maxLength = 0;

        for (String emoji : emojis) {
            if (input.contains(emoji) && emoji.length() > maxLength) {
                emojiResult = Optional.of(emoji);
                maxLength = emoji.length();
            }
        }
        return emojiResult;
    }

}