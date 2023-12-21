-- MySQL dump 10.13  Distrib 8.0.23, for Linux (x86_64)
--
-- Host: localhost    Database: Spacey
-- ------------------------------------------------------
-- Server version	8.0.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `Spacey`
--

CREATE TABLE IF NOT EXISTS `SpaceUsers` (
    `guildId` bigint unsigned NOT NULL, 
    `userId` bigint unsigned NOT NULL, 
    `money` bigint NOT NULL DEFAULT '10000', 
    `gold` bigint NOT NULL DEFAULT '0', 
    `iron` bigint NOT NULL DEFAULT '500', 
    `stone` bigint NOT NULL DEFAULT '1000', 
    `dailyStreak` bigint unsigned NOT NULL DEFAULT '0', 
    `dailyReceived` date NOT NULL DEFAULT '1000-01-01', 
    `reminderSent` tinyint(1) NOT NULL DEFAULT '0', 
    `dailyValuesUpdated` date NOT NULL DEFAULT '1000-01-01', 
    `dailyVCMinutes` int unsigned NOT NULL DEFAULT '0', 
    `moneyGiven` bigint NOT NULL DEFAULT '0', 
    `dailyIncome` bigint NOT NULL DEFAULT '0', 
    `boosters` int NOT NULL DEFAULT '0', 
    `onServer` tinyint(1) DEFAULT '1', 
    PRIMARY KEY (guildId, userId) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `PowerPlantGuilds` (
    `guildId` bigint unsigned NOT NULL, 
    `prefix` char(8) NOT NULL DEFAULT 'S.', 
    `locale` char(7) NOT NULL DEFAULT 'en_us', 
    `spaceStatus` char(7) DEFAULT 'ACTIVE', 
    `spaceUfoSpawning` tinyint(1) NOT NULL DEFAULT '1', 
    `spaceUfoDespawning` tinyint(1) NOT NULL DEFAULT '1', 
    `spaceReminders` tinyint(1) NOT NULL DEFAULT '1', 
    `spaceMoneyGivenLimit` tinyint(1) NOT NULL DEFAULT '1', 
    `announcementChannelId` bigint unsigned NOT NULL DEFAULT '0', 
    `announcementActive` tinyint(1) NOT NULL DEFAULT '0', 
    `big` tinyint(1) NOT NULL DEFAULT '0', 
    `trial_started` timestamp, 
    PRIMARY KEY (guildId) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `StatsServerCount` (
    `date` date NOT NULL, 
    `count` int unsigned NOT NULL, 
    PRIMARY KEY (`date`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `CommandUsages` (
    `command` varchar(25) NOT NULL, 
    `usages` int NOT NULL DEFAULT '1', 
    PRIMARY KEY (`command`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `StatsCommandUsages` (
    `date` date NOT NULL, 
    `count` int unsigned NOT NULL, 
    PRIMARY KEY (`date`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `StatsUpvotes` (
    `date` date NOT NULL, 
    `totalUpvotes` int unsigned NOT NULL, 
    `monthlyUpvotes` int unsigned NOT NULL, 
    PRIMARY KEY (`date`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `BannedUsers` (
    `userId` bigint unsigned NOT NULL, 
    PRIMARY KEY (`userId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `Upvotes` (
    `userId` bigint unsigned NOT NULL, 
    `lastDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    `lastDateReminder` timestamp DEFAULT (CURRENT_TIMESTAMP - INTERVAL '1' DAY), 
    `totalUpvotes` int unsigned NOT NULL, 
    PRIMARY KEY (`userId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `StaticReactionMessages` (
    `guildId` bigint unsigned NOT NULL, 
    `channelId` bigint unsigned NOT NULL, 
    `messageId` bigint unsigned NOT NULL, 
    `command` varchar(50) NOT NULL, 
    PRIMARY KEY (`messageId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `Version` (
    `version` char(10) NOT NULL, 
    `date` timestamp NOT NULL, 
    PRIMARY KEY (`version`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `SpaceUserPowerUp` (
    `guildId` bigint unsigned NOT NULL, 
    `userId` bigint unsigned NOT NULL, 
    `categoryId` int unsigned NOT NULL, 
    `powerUpId` int unsigned NOT NULL DEFAULT '0', 
    `level` int unsigned NOT NULL DEFAULT '1', 
    PRIMARY KEY (`guildId`, `userId`, `categoryId`, `powerUpId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `SpaceIgnoredChannels` (
    `guildId` bigint unsigned NOT NULL, 
    `channelId` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guildId`, `channelId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `CommandControlOff` (
    `guildId` bigint unsigned NOT NULL, 
    `element` varchar(50) NOT NULL, 
    PRIMARY KEY (`guildId`, `element`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `WhiteListedChannels` (
    `guildId` bigint unsigned NOT NULL, 
    `channelId` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guildId`, `channelId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `TempChannel` (
    `guildId` bigint unsigned NOT NULL, 
    `active` tinyint(1) NOT NULL DEFAULT '0', 
    `nameMask` varchar(50) NOT NULL, 
    `locked` tinyint(1) NOT NULL DEFAULT '0', 
    PRIMARY KEY (`guildId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `TempChannelParentChannels` (
    `guildId` bigint unsigned NOT NULL, 
    `channelId` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guildId`, `channelId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `TempChannelChildChannels` (
    `guildId` bigint unsigned NOT NULL, 
    `channelId` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guildId`, `channelId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `BasicRoles` (
    `guildId` bigint unsigned NOT NULL, 
    `roleId` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guildId`, `roleId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `Counter` (
    `guildId` bigint unsigned NOT NULL, 
    `active` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `currentCount` int unsigned NOT NULL DEFAULT '0', 
    `channelId` bigint unsigned NOT NULL DEFAULT '0', 
    `calculations` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `reactions` tinyint(1) unsigned NOT NULL DEFAULT '1', 
    `deleteWrong` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `singleMessage` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `lastMemberId` bigint unsigned NOT NULL DEFAULT '0', 
    `resetAfterWrong` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `replyMessage` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    PRIMARY KEY (`guildId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `SelectRoles` (
    `guildId` bigint unsigned NOT NULL, 
    `channelId` bigint unsigned NOT NULL, 
    `messageId` bigint unsigned NOT NULL, 
    `title` varchar(250), 
    `description` varchar(1024), 
    `banner` varchar(1024), 
    `thumbnail` varchar(1024), 
    `color` varchar(16), 
    `removeRole` tinyint(1) unsigned NOT NULL DEFAULT '1', 
    `multipleRoles` tinyint(1) unsigned NOT NULL DEFAULT '1', 
    `excluded_roles` tinyint(1) NOT NULL DEFAULT '0', 
    `all_roles` tinyint(1) NOT NULL DEFAULT '0', 
    `custom_message_id` bigint NOT NULL, 
    PRIMARY KEY (`messageId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `SelectRoleConnections` (
    `guildId` bigint unsigned NOT NULL, 
    `messageId` bigint unsigned NOT NULL, 
    `emoji` varchar(64) NOT NULL, 
    `roleId` bigint unsigned NOT NULL, 
    `description` varchar(50), 
    PRIMARY KEY (`messageId`, `roleId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `SelectRoleRestrictedRole` (
    `guild_id` bigint unsigned NOT NULL, 
    `message_id` bigint unsigned NOT NULL, 
    `role_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`message_id`, `role_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `WelcomeMessages` (
    `guildId` bigint unsigned NOT NULL, 
    `title` varchar(250), 
    `description` varchar(1024), 
    `image` varchar(1024), 
    `color` varchar(16), 
    `dmText` varchar(1024), 
    `channelId` bigint unsigned, 
    `active` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `dmActive` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `custom_message_id_guild` bigint NOT NULL, 
    `custom_message_id_dm` bigint NOT NULL, 
    PRIMARY KEY (`guildId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `FarewellMessages` (
    `guildId` bigint unsigned NOT NULL, 
    `title` varchar(250), 
    `description` varchar(1024), 
    `image` varchar(1024), 
    `color` varchar(16), 
    `channelId` bigint unsigned, 
    `active` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    `custom_message_id` bigint NOT NULL, 
    PRIMARY KEY (`guildId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `UserPrivateChannels` (
    `userId` bigint unsigned NOT NULL, 
    `privateChannelId` bigint unsigned NOT NULL, 
    PRIMARY KEY (`userId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `Moderation` (
    `guildId` bigint unsigned NOT NULL, 
    `modLogChannelId` bigint unsigned, 
    auto_kick_enabled tinyint(1) NOT NULL, 
    auto_mute_enabled tinyint(1) NOT NULL, 
    auto_ban_enabled tinyint(1) NOT NULL, 
    warns_auto_kick tinyint(2) NOT NULL, 
    warns_auto_mute tinyint(2) NOT NULL, 
    warns_auto_ban tinyint(2) NOT NULL, 
    days_warns_counted tinyint(3) NOT NULL, 
    PRIMARY KEY (`guildId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `exWarnings` (
    `guildId` bigint unsigned NOT NULL, 
    `userId` bigint unsigned NOT NULL, 
    `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    `requestorUserId` bigint unsigned NOT NULL, 
    `reason` varchar(300) DEFAULT NULL, 
    PRIMARY KEY (`guildId`, `userId`, `time`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `TempBans` (
    `guildId` bigint unsigned NOT NULL, 
    `userId` bigint unsigned NOT NULL, 
    `expires` timestamp NOT NULL, 
    PRIMARY KEY (`guildId`, `userId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `BasicRolesSettings` (
    `guildId` bigint unsigned NOT NULL, 
    `bots` tinyint(1) unsigned NOT NULL DEFAULT '1', 
    PRIMARY KEY (`guildId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `SpaceUserIncomes` (
    `guildId` bigint unsigned NOT NULL, 
    `userId` bigint unsigned NOT NULL, 
    `time` timestamp NOT NULL, 
    `moneyGrowth` bigint unsigned NOT NULL DEFAULT '0', 
    PRIMARY KEY (`guildId`, `userId`, `time`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `StatsChannels` (
    `guild_id` bigint unsigned NOT NULL, 
    `vc_id` bigint unsigned NOT NULL, 
    `name_mask` varchar(50) NOT NULL, 
    PRIMARY KEY (`guild_id`, `vc_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `Reminders` (
    `userId` bigint unsigned NOT NULL, 
    `reminder` varchar(20) NOT NULL, 
    `errors` tinyint unsigned NOT NULL DEFAULT '0', 
    PRIMARY KEY (`userId`, `reminder`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `UserSettings` (
    `userId` bigint unsigned NOT NULL, 
    `locale` varchar(7) NOT NULL, 
    PRIMARY KEY (`userId`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `TicketPanel` (
    `guild_id` bigint unsigned NOT NULL, 
    `panel_id` bigint unsigned NOT NULL, 
    `counter` SMALLINT unsigned NOT NULL, 
    `title` varchar(250) NOT NULL, 
    `ping_staff` tinyint(1) NOT NULL DEFAULT '1', 
    `ticket_message` varchar(4096), 
    `panel_message` varchar(4096), 
    `log_channel` bigint unsigned, 
    `category_open` bigint unsigned, 
    `category_archived` bigint unsigned, 
    `name_mask_open` varchar(50), 
    `name_mask_archived` varchar(50), 
    `member_can_delete` tinyint(1) NOT NULL, 
    `member_can_archive` tinyint(1) NOT NULL, 
    `assignment` tinyint(1) NOT NULL, 
    `close_confirmation` tinyint(1) NOT NULL, 
    `dm_log` tinyint(1) NOT NULL, 
    `color` varchar(16), 
    `custom_message_id_panel` bigint NOT NULL, 
    `custom_message_id_ticket` bigint NOT NULL, 
    `button_label` varchar(80), 
    PRIMARY KEY (`panel_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `TicketPanelStaffRoles` (
    `guild_id` bigint unsigned NOT NULL, 
    `panel_id` bigint unsigned NOT NUll, 
    `role_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`panel_id`, `role_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `TicketChannel` (
    `guild_id` bigint unsigned NOT NULL, 
    `panel_id` bigint unsigned NOT NULL, 
    `ticket_channel_id` bigint unsigned NOT NULL, 
    `ticket_message_id` bigint unsigned NOT NULL, 
    `announcement_channel_id` bigint unsigned, 
    `requestor_id` bigint unsigned NOT NULL, 
    `count` smallint unsigned NOT NULL, 
    `creation_time` timestamp NOT NULL, 
    `state` tinyint(1) unsigned NOT NULL, 
    PRIMARY KEY (`ticket_channel_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `TicketAssignment` (
    `guild_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`channel_id`, `user_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `TicketClient` (
    `guild_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`channel_id`, `user_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `TicketPanelMessage` (
    `guild_id` bigint unsigned NOT NULL, 
    `panel_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned NOT NULL, 
    `message_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`message_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `Partner` (
    `application_id` bigint unsigned NOT NULL, 
    `partner_type` tinyint(1) unsigned NOT NULL, 
    `creation_time` timestamp NOT NULL, 
    `short_description` varchar(256), 
    `long_description` varchar(4096), 
    `invite_url` varchar(1000), 
    `verification_state` tinyint(1) NOT NULL, 
    `advert_guild_id` bigint unsigned NOT NULL, 
    `advert_channel_id` bigint unsigned NOT NULL, 
    `advert_message_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`application_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `PartnerOwner` (
    `user_id` bigint unsigned NOT NULL, 
    `application_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`user_id`, `application_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `PartnerLanguage` (
    `application_id` bigint unsigned NOT NULL, 
    `language` tinyint(2) NOT NULL, 
    PRIMARY KEY (`language`, `application_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `PartnerVerificationListener` (
    `application_id` bigint unsigned NOT NULL, 
    `message_id` bigint unsigned NOT NULL, 
    `type` tinyint(1) unsigned NOT NULL, 
    PRIMARY KEY (`message_id`) 
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `AutoPublish` (
    `guild_id` bigint unsigned NOT NULL, 
    `active` tinyint(1) NOT NULL DEFAULT '0', 
    PRIMARY KEY (`guild_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `AutoPublishExcludedChannels` (
    `guild_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guild_id`, `channel_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `Poll` (
    `guild_id` bigint unsigned NOT NULL, 
    `message_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    `max_selections` int unsigned NOT NULL, 
    `topic` varchar(4096) NOT NULL, 
    PRIMARY KEY (`message_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `PollVote` (
    `guild_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    `poll_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`user_id`, `poll_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `PollVoteEntry` (
    `user_id` bigint unsigned NOT NULL, 
    `poll_id` bigint unsigned NOT NULL, 
    `entry` int unsigned NOT NULL, 
    PRIMARY KEY (`user_id`, `poll_id`, `entry`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


    
CREATE TABLE IF NOT EXISTS `PollOption` (
    `id` tinyint (2) NOT NULL, 
    `name` varchar(200) NOT NULL, 
    `poll_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`name`, `poll_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `MessageRaffleSlot` (
    `guild_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned, 
    `message_id` bigint unsigned, 
    `winners` int unsigned, 
    `prize` varchar(150), 
    `luck` int unsigned, 
    `start_time` timestamp, 
    `minutes` int unsigned, 
    `rolled` tinyint unsigned NOT NULL, 
    `dm` tinyint(1) unsigned NOT NULL, 
    `vote_multiplier` tinyint(1) unsigned NOT NULL, 
    `image` varchar(2096), 
    `registration` tinyint(1) unsigned NOT NULL DEFAULT '0', 
    PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `MessageRaffleEntry` (
    `guild_id` bigint unsigned NOT NULL, 
    `message_raffle_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    `entry_count` float(24) unsigned NOT NULL, 
    `registered` tinyint(1) unsigned NOT NULL, 
    PRIMARY KEY (`message_raffle_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `MessageRaffleChannel` (
    `guild_id` bigint NOT NULL, 
    `message_raffle_id` bigint NOT NULL, 
    `channel_id` bigint NOT NULL, 
    PRIMARY KEY (`message_raffle_id`, `channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `MessageRaffleExcludedRole` (
    `guild_id` bigint unsigned NOT NULL, 
    `message_raffle_id` bigint unsigned NOT NULL, 
    `role_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`message_raffle_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `MessageRaffleMultipleEntryRole` (
    `guild_id` bigint unsigned NOT NULL, 
    `message_raffle_id` bigint unsigned NOT NULL, 
    `role_id` bigint unsigned NOT NULL, 
    `multiplier` float(24) unsigned NOT NULL, 
    PRIMARY KEY (`message_raffle_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `GameStatistics` (
    `game` varchar(10) NOT NULL, 
    `won` tinyint(1) NOT NULL, 
    `value` double NOT NULL, 
    PRIMARY KEY (`game`,`won`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


# manual users

CREATE TABLE IF NOT EXISTS `PremiumUser` (
    `user_id` bigint unsigned NOT NULL, 
    `tier` int unsigned NOT NULL, 
    `expires` timestamp NOT NULL, 
    PRIMARY KEY (`user_id`) 
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `PremiumGuild` (
    `guild_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    `slot_id` tinyint(2) unsigned NOT NULL, 
    `time_unlocked` timestamp NOT NULL, 
    `embed_color_hex` varchar(7), 
    PRIMARY KEY (`guild_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `PatreonUserOld` (
      `user_id` bigint unsigned NOT NULL,
      PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

    
CREATE TABLE IF NOT EXISTS `Message` (
    `guild_id` bigint unsigned NOT NULL, 
    `id` bigint NOT NULL, 
    `mention` tinyint(1) NOT NULL, 
    `description` varchar(2000), 
    PRIMARY KEY (`guild_id`, `id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `Embed` (
    `guild_id` bigint unsigned NOT NULL, 
    `message_id` bigint NOT NULL, 
    `id` bigint unsigned NOT NULL, 
    `url` varchar(2000), 
    `title` varchar(256), 
    `description` varchar(4096), 
    `color` varchar(7), 
    `thumbnail_url` TEXT,  # 2000
    `image_url` TEXT,  # 2000
    `author_name` varchar(256), 
    `author_url` varchar(2000), 
    `author_icon_url` varchar(2000), 
    `footer_text` varchar(2048), 
    `footer_icon_url` varchar(2000), 
    `timestamp` timestamp, 
    `position` tinyint(2) unsigned NOT NULL, 
    PRIMARY KEY (`guild_id`, `message_id`, `id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `EmbedField` (
    `guild_id` bigint unsigned NOT NULL, 
    `embed_id` bigint NOT NULL, 
    `message_id` bigint NOT NULL, 
    `position` tinyint(1) NOT NULL, 
    `title` varchar(256), 
    `value` varchar(6000), 
    `inline` tinyint(1) unsigned NOT NULL, 
    PRIMARY KEY (`embed_id`, `message_id`, `position`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `DiscordMessage` (
    `guild_id` bigint unsigned NOT NULL, 
    `custom_message_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned NOT NULL, 
    `message_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`custom_message_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `SpecialRole` (
    `user_id` bigint unsigned NOT NULL, 
    `role_ordinal` tinyint(2) unsigned NOT NULL, 
    PRIMARY KEY (`user_id`, `role_ordinal`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `AdventCalendar` (
    `user_id` bigint unsigned NOT NULL, 
    `last_day` tinyint(2) unsigned NOT NULL, 
    PRIMARY KEY (`user_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `AdventCalendarCollected` (
    `user_id` bigint unsigned NOT NULL, 
    `ordinal` tinyint(2) unsigned NOT NULL, 
    PRIMARY KEY (`user_id`, `ordinal`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `WarnRole` (
    `guild_id` bigint unsigned NOT NULL, 
    `role_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (guild_id, role_id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `UfoChannel` (
    `guild_id` bigint unsigned NOT NULL, 
    `channel_id` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guild_id`, `channel_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `SpaceRocket` (
    `guild_id` bigint unsigned NOT NULL,
    `user_id` bigint unsigned NOT NULL, 
    `size_ordinal` tinyint(1) unsigned NOT NULL, 
    `amount` bigint unsigned NOT NULL, 
    `amount_lifetime` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guild_id`, `user_id`, `size_ordinal`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `SpacePlanet` (
    `guild_id` bigint unsigned NOT NULL,
    `user_id` bigint unsigned NOT NULL, 
    `size_ordinal` tinyint(1) unsigned NOT NULL, 
    `amount` bigint unsigned NOT NULL, 
    PRIMARY KEY (`guild_id`, `user_id`, `size_ordinal`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `NewsFeed` (
    `user_id` bigint unsigned NOT NULL, 
    `id` bigint unsigned NOT NULL, 
    `time` timestamp NOT NULL, 
    `read` tinyint(1) NOT NULL, 
    `title` varchar (256) NOT NULL, 
    `news` varchar(2048) NOT NULL, 
    PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `NewsFeedUser` (
    `user_id` bigint unsigned NOT NULL, 
    `dm` tinyint(1) NOT NULL, 
    PRIMARY KEY (`user_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `SpaceMission` (
    `guild_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    `rocket_size_ordinal` tinyint(1) UNSIGNED NOT NULL,
    `planet_size_ordinal` tinyint(1) UNSIGNED, 
    `timestamp_end` timestamp NOT NULL, 
    `id` bigint NOT NULL, 
    PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

    
CREATE TABLE IF NOT EXISTS `SpacePlanet` (
    `guild_id` bigint unsigned NOT NULL, 
    `user_id` bigint unsigned NOT NULL, 
    `planet_id` bigint unsigned NOT NULL, 
    `planet_size` tinyint(1) unsigned NOT NULL, 
    PRIMARY KEY (`planet_id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;