package mysql;

import core.assets.GuildAsset;

import java.util.Observable;

public abstract class DataWithGuild extends Observable implements GuildAsset {

	private final long guildId;

	public DataWithGuild(long guildId) {
		this.guildId = guildId;
	}

    @Override
	public long getGuildId() {
		return guildId;
	}

}