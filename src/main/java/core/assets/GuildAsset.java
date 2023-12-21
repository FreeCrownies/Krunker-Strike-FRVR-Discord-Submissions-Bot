package core.assets;

import core.ShardManager;
import mysql.modules.guild.DBGuild;
import mysql.modules.guild.GuildData;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Optional;

public interface GuildAsset {

    long getGuildId();

    default Optional<Guild> getGuild() {
        return ShardManager.getLocalGuildById(getGuildId());
    }

    default GuildData getGuildData() {
        return DBGuild.getInstance().retrieve(getGuildId());
    }

}