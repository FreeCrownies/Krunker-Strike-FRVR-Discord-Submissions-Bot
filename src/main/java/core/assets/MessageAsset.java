package core.assets;

import core.utils.BotPermissionUtil;
import net.dv8tion.jda.api.entities.Message;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public interface MessageAsset extends GuildMessageChannelAsset {

    long getMessageId();

    default CompletableFuture<Message> retrieveMessage() {
        return getGuildMessageChannel().map(channel -> {
            if (BotPermissionUtil.canReadHistory(channel)) {
                return channel.retrieveMessageById(getMessageId()).submit();
            } else {
                return null;
            }
        }).orElseGet(() -> CompletableFuture.failedFuture(new NoSuchElementException("No text channel")));
    }

}