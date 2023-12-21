package mysql.modules.guild;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import constants.Language;
import core.MainLogger;
import core.ShardManager;
import mysql.DBDataLoad;
import mysql.DBMain;
import mysql.DBObserverMapCache;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Locale;

public class DBGuild extends DBObserverMapCache<Long, GuildData> {

    private static final DBGuild INSTANCE = new DBGuild();
    private final Cache<Long, Boolean> removedGuildIds = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    private DBGuild() {
    }

    public static DBGuild getInstance() {
        return INSTANCE;
    }

    @Override
    public GuildData load(Long guildId) throws Exception {
        int shard = ShardManager.getResponsibleShard(guildId);
        if (shard < ShardManager.getShardIntervalMin() || shard > ShardManager.getShardIntervalMax()) {
            MainLogger.get().error("Invalid guild");
        }

        boolean guildPresent = ShardManager.getLocalGuildById(guildId).isPresent();
        if (guildPresent) {
            removedGuildIds.invalidate(guildId);
        }

        return new DBDataLoad<GuildData>("PowerPlantGuilds", "prefix, locale",
                "guildId = ?", preparedStatement -> preparedStatement.setLong(1, guildId)
        ).getOrDefault(resultSet -> new GuildData(
                        guildId,
                        resultSet.getString(1),
                        new Locale(resultSet.getString(2))
                ), () -> new GuildData(
                        guildId,
                        "S.",
                        Language.EN.getLocale()
                )
        );
    }

    public boolean containsGuildId(long guildId) {
        return !removedGuildIds.asMap().containsKey(guildId);
    }

    @Override
    protected void save(GuildData guildData) {
        try {
            DBMain.getInstance().update("REPLACE INTO PowerPlantGuilds (guildId, prefix, locale) VALUES (?, ?, ?)",
                    preparedStatement -> {
                        preparedStatement.setLong(1, guildData.getGuildId());
                        preparedStatement.setString(2, guildData.getPrefix());
                        preparedStatement.setString(3, guildData.getLocale().getDisplayName());
                    });
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
            guildData.notifyObservers();
        }
    }

    public void remove(long guildId) {
        removedGuildIds.put(guildId, true);
        DBMain.getInstance().asyncUpdate("DELETE FROM PowerPlantGuilds WHERE guildId = ?;", preparedStatement -> preparedStatement.setLong(1, guildId));
        DBMain.getInstance().invalidateGuildId(guildId);
    }

}
