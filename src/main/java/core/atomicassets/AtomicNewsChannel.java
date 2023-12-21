package core.atomicassets;

import core.CustomObservableList;
import core.ShardManager;
import mysql.modules.guild.DBGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AtomicNewsChannel implements MentionableAtomicAsset<NewsChannel> {

    private final long guildId;
    private final long channelId;

    public AtomicNewsChannel(long guildId, long channelId) {
        this.guildId = guildId;
        this.channelId = channelId;
    }

    public AtomicNewsChannel(NewsChannel channel) {
        channelId = channel.getIdLong();
        guildId = channel.getGuild().getIdLong();
    }

    public static List<AtomicNewsChannel> from(List<NewsChannel> channels) {
        return channels.stream()
                .map(AtomicNewsChannel::new)
                .collect(Collectors.toList());
    }

    public static List<NewsChannel> to(List<AtomicNewsChannel> channels) {
        return channels.stream()
                .map(AtomicNewsChannel::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public static CustomObservableList<AtomicNewsChannel> transformIdList(Guild guild, CustomObservableList<Long> list) {
        return list.transform(
                id -> new AtomicNewsChannel(guild.getIdLong(), id),
                AtomicNewsChannel::getIdLong
        );
    }

    @Override
    public long getIdLong() {
        return channelId;
    }

    @Override
    public Optional<NewsChannel> get() {
        return ShardManager.getLocalGuildById(guildId)
                .map(guild -> guild.getChannelById(NewsChannel.class, channelId));
    }

    @Override
    public Locale getLocale() {
        return DBGuild.getInstance().retrieve(guildId).getLocale();
    }

    @Override
    public Optional<String> getPrefixedNameRaw() {
        return get().map(c -> "#" + c.getName());
    }

    @Override
    public Optional<String> getNameRaw() {
        return get().map(NewsChannel::getName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomicNewsChannel that = (AtomicNewsChannel) o;
        return channelId == that.channelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId);
    }

}