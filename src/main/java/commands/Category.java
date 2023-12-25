package commands;

import java.util.Arrays;

public enum Category {

    DM("dm", "⚙️", true);

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
