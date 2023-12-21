package core.assets;

import core.MemberCacheController;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

public interface MemberAsset extends GuildAsset {

	long getUserId();

	default Optional<Member> getMember() {
		return getGuild()
				.map(guild -> MemberCacheController.getInstance().loadMember(guild, getUserId()).join());
	}

}