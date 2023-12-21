package core;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DiscordFile {

    private static final Guild guild;
    private static final TextChannel channel;

    static {
        guild = ShardManager.getLocalGuildById(809878088963850240L).orElseThrow();
        channel = guild.getTextChannelById(921796158148444213L);
        if (channel == null) {
            MainLogger.get().error("Unable to find image channel!");
        }
    }

    File file;
    private String url;
    private Message.Attachment attachment;
    private InputStream inputStream;
    private String fileName;

    public DiscordFile(Message.Attachment attachment) {
        this.attachment = attachment;
    }

    public DiscordFile(URL url) throws IOException {
        this.inputStream = url.openStream();
        this.fileName = url.getFile();
    }

    public boolean upload() {
        try {
            if (attachment != null) {
                file = attachment.downloadToFile().get();
                this.url = channel.sendFiles(FileUpload.fromData(file)).complete().getAttachments().get(0).getUrl();
                file.delete();
            } else {
                this.url = channel.sendFiles(FileUpload.fromData(inputStream, fileName)).complete().getAttachments().get(0).getUrl();
            }
            return true;
        } catch (Exception e) {
            MainLogger.get().error("Error while uploading file", e);
            return false;
        }
    }

    public String getUrl() {
        return url;
    }
}
