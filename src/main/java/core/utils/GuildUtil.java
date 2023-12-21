package core.utils;

import core.MemberCacheController;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class GuildUtil {

    public static int getBotCount(Guild guild) {
        return getBotCount(MemberCacheController.getInstance().loadMembersFull(guild).join());
    }

    public static int getBotCount(List<Member> members) {
        return (int) members.stream()
                .map(Member::getUser)
                .filter(User::isBot)
                .count();
    }

    public static int getUserCount(Guild guild) {
        return getUserCount(MemberCacheController.getInstance().loadMembersFull(guild).join());
    }

    public static int getUserCount(List<Member> members) {
        return (int) members.stream()
                .map(Member::getUser)
                .filter(user -> !user.isBot())
                .count();
    }

}