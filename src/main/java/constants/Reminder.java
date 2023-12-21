package constants;

public enum Reminder {

    UPVOTE,
    DAILY;

    public static Reminder parse(String name) {
        for (Reminder reminder : values()) {
            if (reminder.name().equalsIgnoreCase(name)) return reminder;
        }
        return null;
    }

}
