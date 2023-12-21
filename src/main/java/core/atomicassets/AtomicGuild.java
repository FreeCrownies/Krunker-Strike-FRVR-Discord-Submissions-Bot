package core.atomicassets;

import core.ShardManager;
import core.TextManager;
import mysql.modules.guild.DBGuild;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class AtomicGuild implements AtomicAsset<Guild> {

    private final long guildId;

    public AtomicGuild(long guildId) {
        this.guildId = guildId;
    }

    public AtomicGuild(Guild guild) {
        guildId = guild.getIdLong();
    }

    @Override
    public long getIdLong() {
        return guildId;
    }

    @Override
    public Optional<Guild> get() {
        return ShardManager.getLocalGuildById(guildId);
    }

    public Locale getLocale() {
        return DBGuild.getInstance().retrieve(guildId).getLocale();
    }

    public Optional<String> getNameRaw() {
        return get().map(Guild::getName);
    }

    public String getName() {
        return getNameRaw()
                .orElseGet(() -> TextManager.getString(getLocale(), TextManager.GENERAL, "not_found", String.valueOf(getIdLong())));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomicGuild that = (AtomicGuild) o;
        return guildId == that.guildId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId);
    }

}