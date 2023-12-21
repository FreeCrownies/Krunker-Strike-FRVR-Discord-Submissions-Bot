package commands;

import java.util.Arrays;

public enum Category {

    // GIMMICKS("gimmicks", "\uD83D\uDC8D", true),
    CONFIGURATION("configuration", "⚙️", true),
    UTILITY("utility", "\uD83D\uDD27", true),
    MODERATION("moderation", "👮", true),
    INFORMATION("information", "ℹ", true),
    SPACE("space_category", "🚀", true),
    SPACE_SETTINGS("space_settings", "⚙️", true),
    CASINO("casino", "\uD83D\uDCB8", true);
    // INTERACTIONS("interactions", "\uD83E\uDEC2", true);

    private final String id;
    private final String emoji;
    private final boolean independent;

    Category(String id, String emoji, boolean independent) {
        this.id = id;
        this.emoji = emoji;
        this.independent = independent;
    }

    public static Category[] independentValues() {
        return Arrays.stream(values())
                .filter(Category::isIndependent)
                .toArray(Category[]::new);
    }

    public String getId() {
        return id;
    }

    public String getEmoji() {
        return emoji;
    }

    public boolean isIndependent() {
        return independent;
    }

}
