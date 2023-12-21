package core.utils;

import core.LocalFile;
import core.MainLogger;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class FileUtil {

    public static void downloadFile(String urlString, String extension, Consumer<File> consumer) {
        try {
            URL url = new URL(urlString);
            File file = new File(ProgramUtil.generateId() + "." + extension);
            FileUtils.copyURLToFile(url, file);
            consumer.accept(file);
            file.delete();
        } catch (SecurityException | IOException e) {
            MainLogger.get().error("Error while downloading file", e);
        }
    }

    public static String getStringFromUrl(String urlString) {
        try {
            BufferedInputStream in = new BufferedInputStream(new URL(urlString).openStream());
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Deprecated
    public static boolean downloadImageAttachment(Message.Attachment messageAttachment, LocalFile localFile) {
        try {
            messageAttachment.downloadToFile(localFile).get();
            return true;
        } catch (ExecutionException | InterruptedException e) {
            MainLogger.get().error("Message attachment download exception", e);
            return false;
        }
    }

}
