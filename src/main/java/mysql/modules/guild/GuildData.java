package mysql.modules.guild;

import core.utils.BotPermissionUtil;
import mysql.DataWithGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuildData extends DataWithGuild {

    private final long guildId;
    private String prefix;
    private Locale locale;

    public GuildData(long guildId, String prefix, Locale locale) {
        super(guildId);
        this.guildId = guildId;
        this.prefix = prefix;
        this.locale = locale;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        if (this.prefix == null || !this.prefix.equals(prefix)) {
            this.prefix = prefix;
            setChanged();
            notifyObservers();
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        if (this.locale == null || !this.locale.equals(locale)) {
            this.locale = locale;
            setChanged();
            notifyObservers();
        }
    }

    public boolean isSaved() {
        return DBGuild.getInstance().containsGuildId(guildId);
    }

    private StandardGuildMessageChannel getDefaultAnnouncementChannel(Guild guild) {
        List<StandardGuildMessageChannel> writeableChannels = guild.getChannels().stream()
                .filter(channel -> channel instanceof StandardGuildMessageChannel)
                .map(channel -> (StandardGuildMessageChannel) channel)
                .filter(BotPermissionUtil::canWriteEmbed)
                .collect(Collectors.toList());
        return Optional.ofNullable((StandardGuildMessageChannel) guild.getCommunityUpdatesChannel()).orElse(writeableChannels.isEmpty() ? null : writeableChannels.get(0));
    }

}
