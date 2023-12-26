package mysql;

import java.sql.SQLException;

public interface SQLSchema {

    static void onCreate() throws SQLException, InterruptedException {

        DBMain.getInstance().asyncUpdate(
                "CREATE TABLE IF NOT EXISTS `PowerPlantGuilds` (" +
                        "`guildId` bigint unsigned NOT NULL, " +
                        "`prefix` char(8) NOT NULL DEFAULT 'S.', " +
                        "`locale` char(7) NOT NULL DEFAULT 'en_us', " +
                        "PRIMARY KEY (guildId) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
        );

        DBMain.getInstance().update(
                "CREATE TABLE IF NOT EXISTS `BannedUsers` (" +
                        "`userId` bigint unsigned NOT NULL, " +
                        "PRIMARY KEY (`userId`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
        );

        DBMain.getInstance().update(
                "CREATE TABLE IF NOT EXISTS `StaticReactionMessages` (" +
                        "`guildId` bigint unsigned NOT NULL, " +
                        "`channelId` bigint unsigned NOT NULL, " +
                        "`messageId` bigint unsigned NOT NULL, " +
                        "`command` varchar(50) NOT NULL, " +
                        "PRIMARY KEY (`messageId`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
        );

        DBMain.getInstance().asyncUpdate(
                "CREATE TABLE IF NOT EXISTS `UserPrivateChannels` (" +
                        "`userId` bigint unsigned NOT NULL, " +
                        "`privateChannelId` bigint unsigned NOT NULL, " +
                        "PRIMARY KEY (`userId`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
        );

        DBMain.getInstance().asyncUpdate(
                "CREATE TABLE IF NOT EXISTS `SlashPermissions` (" +
                        "`guild_id` bigint unsigned NOT NULL," +
                        "`command` varchar(50) COLLATE utf8mb4_general_ci NOT NULL, " +
                        "`object_id` bigint unsigned NOT NULL, " +
                        "`object_type` tinyint unsigned NOT NULL, " +
                        "`allowed` tinyint unsigned NOT NULL, " +
                        "PRIMARY KEY (`guild_id`, `command`, `object_id`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
        );

        DBMain.getInstance().asyncUpdate(
                "CREATE TABLE IF NOT EXISTS `Submissions` (" +
                        "`user_id` bigint unsigned NOT NULL, " +
                        "`message_id` bigint unsigned NOT NULL, " +
                        "`message_id_video` bigint unsigned, " +
                        "`type` tinyint(1) unsigned NOT NULL, " +
                        "`media_url` varchar(500) NOT NULL, " +
                        "`description` varchar(1000), " +
                        "PRIMARY KEY (`user_id`, `type`) " +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
        );


//        DBMain.getInstance().asyncUpdate(
//                "CREATE TABLE IF NOT EXISTS `` (" +
//                        "PRIMARY KEY () " +
//                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;"
//        );


    }

}
