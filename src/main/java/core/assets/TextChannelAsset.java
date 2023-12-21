package core.assets;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Optional;

public interface TextChannelAsset extends GuildAsset {

    long getTextChannelId();

    default Optional<TextChannel> getTextChannel() {
        return getGuild().map(guild -> guild.getTextChannelById(getTextChannelId()));
    }

}