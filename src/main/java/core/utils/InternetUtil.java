package core.utils;

import com.google.common.net.UrlEscapers;
import core.MainLogger;

import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

public final class InternetUtil {

    private InternetUtil() {
    }

    public static boolean urlContainsImage(String url) {
        return url.endsWith("jpeg") || url.endsWith("jpg") || url.endsWith("png") || url.endsWith("bmp") || url.endsWith("gif");
    }

    public static boolean stringHasURL(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        return url.contains("http://") || url.contains("https://") || url.contains("www.");
    }

    public static boolean stringIsURL(String url) {
        return url != null && Pattern.compile("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})").matcher(url).matches();
    }

    public static boolean checkConnection() {
        try {
            URL url = new URL("https://www.google.com/");
            URLConnection connection = url.openConnection();
            connection.connect();

            return true;
        } catch (Throwable e) {
            MainLogger.get().error("Could not create connection to google", e);
        }
        return false;
    }

    public static String escapeForURL(String url) {
        return UrlEscapers.urlFragmentEscaper().escape(url);
    }

}