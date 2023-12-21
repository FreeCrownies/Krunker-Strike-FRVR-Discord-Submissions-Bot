package constants;

import java.util.Locale;
import java.util.NoSuchElementException;

public enum Language {

    EN(new Locale("en_us"), "🇬🇧", false),
    DE(new Locale("de_de"), "🇩🇪", false);

    public static Language from(Locale locale) {
        for (Language value : Language.values()) {
            if (value.getLocale().getDisplayName().equalsIgnoreCase(locale.getDisplayName())) {
                return value;
            }
        }
        throw new NoSuchElementException("Invalid locale");
    }

    private final Locale locale;
    private final String flag;
    private final boolean deepLGenerated;

    Language(Locale locale, String flag, boolean deepLGenerated) {
        this.locale = locale;
        this.flag = flag;
        this.deepLGenerated = deepLGenerated;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getFlag() {
        return flag;
    }

    public boolean isDeepLGenerated() {
        return deepLGenerated;
    }

}