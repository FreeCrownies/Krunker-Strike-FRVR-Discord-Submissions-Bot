package core.assets;


import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public interface GuildMessageChannelAsset extends GuildAsset {

    long getStandardGuildMessageChannelId();

    default Optional<GuildMessageChannel> getGuildMessageChannel() {
        return getGuild().map(guild -> (GuildMessageChannel) guild.getGuildChannelById(getStandardGuildMessageChannelId()));
    }

}
