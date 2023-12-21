package core.utils;

import constants.AssetIds;
import constants.MemberIds;
import net.dv8tion.jda.api.entities.Member;

import java.util.Arrays;

public class MemberPermissionUtil {

	public static boolean isDeveloper(Member member) {
		return isDeveloper(member.getIdLong());
	}

	public static boolean isDeveloper(long memberId) {
		boolean isDeveloper = false;
		for (long id : MemberIds.DEVELOPERS) {
			if (memberId == id) {
				isDeveloper = true;
				break;
			}
		}
		return isDeveloper;
	}

	public static boolean isBotAdmin(long userId) {
		return Arrays.asList(AssetIds.BOT_ADMINS).contains(userId);
	}

	public static boolean isSupporter(long memberId) {
		boolean isSupporter = false;
		for (long id : MemberIds.SUPPORTERS) {
			if (memberId == id) {
				isSupporter = true;
				break;
			}
		}
		return isSupporter;
	}
	
}
