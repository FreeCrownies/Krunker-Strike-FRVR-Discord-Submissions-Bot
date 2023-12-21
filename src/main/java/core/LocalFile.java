package core;

import constants.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Deprecated
public class LocalFile extends File {

    private String fileName = null;


    public LocalFile(Directory directory, @NotNull String fileName) {
        super(Settings.ROOT_DIR + "/" + directory.getPath(), fileName);
        if (directory == Directory.CDN) {
            this.fileName = fileName;
        }
    }

    public LocalFile(@NotNull String pathname) {
        super(Settings.ROOT_DIR, pathname);
    }

    public enum Directory {

        CDN ("/data/cdn");

        private final String path;

        Directory(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
}
