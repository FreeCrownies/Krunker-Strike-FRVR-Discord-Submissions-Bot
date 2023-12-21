package core;

import commands.Command;
import commands.SlashCommandManager;
import core.utils.BotPermissionUtil;
import mysql.modules.slashpermissions.DBSlashPermissions;
import mysql.modules.slashpermissions.SlashPermissionsSlot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.privileges.IntegrationPrivilege;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandPermissions {

    public static boolean transferCommandPermissions(Guild guild) {
        Map<String, List<IntegrationPrivilege>> externalMap = guild.retrieveCommandPrivileges().complete().getAsMap();
        HashMap<String, List<SlashPermissionsSlot>> internalMap = new HashMap<>();

        for (String commandId : externalMap.keySet()) {
            System.out.println("commandId: " + commandId);
            String commandName = commandId.equals(guild.getSelfMember().getId())
                    ? ""
                    : SlashCommandManager.getNameFromId(Long.parseLong(commandId));
            System.out.println("commandName: " + commandName);
            if (commandName == null) {
                continue;
            }

            List<SlashPermissionsSlot> internalList = externalMap.get(commandId).stream()
                    .map(e -> {
                        System.out.println("object id: " + e.getId());
                        return new SlashPermissionsSlot(
                                guild.getIdLong(),
                                commandName,
                                e.getIdLong(),
                                mapPermissionType(e.getType()),
                                e.isEnabled()
                        );
                    })
                    .filter(e -> e.getType() != null)
                    .collect(Collectors.toList());
            System.out.println("internalList size: " + internalList.size());
            internalMap.put(commandName, internalList);
        }

        DBSlashPermissions.getInstance().retrieve(guild.getIdLong()).setPermissionMap(internalMap);
        return true;
    }

    public static Boolean hasAccess(Class<? extends Command> clazz, Member member, GuildMessageChannel guildMessageChannel, boolean ignoreAdmin) {
        if (!ignoreAdmin && (BotPermissionUtil.can(member, Permission.ADMINISTRATOR) || member.isOwner())) {
            return true;
        }

        Map<String, List<SlashPermissionsSlot>> permissionMap = DBSlashPermissions.getInstance().retrieve(member.getGuild().getIdLong())
                .getPermissionMap();
        String commandName = SlashCommandManager.findName(clazz);

        if (commandName != null && permissionMap.containsKey(commandName)) {
            return checkCommandAccess(permissionMap.get(commandName), member, guildMessageChannel);
        } else if (permissionMap.containsKey("")) {
            return checkCommandAccess(permissionMap.get(""), member, guildMessageChannel);
        } else {
            return null;
        }
    }

    private static Boolean checkCommandAccess(List<SlashPermissionsSlot> commandPermissions, Member member, GuildMessageChannel guildMessageChannel) {
        Boolean b1 = checkPermissionsRolesAndUsers(commandPermissions, member);
        Boolean b2;
        if (guildMessageChannel == null) {
            b2 = true;
        } else {
            b2 = checkPermissionsChannels(commandPermissions, guildMessageChannel);
        }

        if (b1 == null && b2 == null) {
            return null;
        }

        return Boolean.TRUE.equals(b1) && (b2 == null || b2);
    }

    private static Boolean checkPermissionsRolesAndUsers(List<SlashPermissionsSlot> commandPermissions, Member member) {
        Boolean allowed = null;
        for (SlashPermissionsSlot commandPermission : commandPermissions) {
            if (commandPermission.getType() == SlashPermissionsSlot.Type.USER && commandPermission.getObjectId() == member.getIdLong()) {
                return commandPermission.isAllowed();
            }
            if (commandPermission.getType() == SlashPermissionsSlot.Type.ROLE) {
                if (commandPermission.isDefaultObject()) {
                    if (allowed == null && !commandPermission.isAllowed()) {
                        allowed = false;
                    }
                } else if (member.getRoles().stream().anyMatch(r -> r.getIdLong() == commandPermission.getRoleId())) {
                    if (commandPermission.isAllowed()) {
                        allowed = true;
                    } else if (allowed == null) {
                        allowed = false;
                    }
                }
            }
        }
        return allowed;
    }

    private static Boolean checkPermissionsChannels(List<SlashPermissionsSlot> commandPermissions, GuildMessageChannel guildMessageChannel) {
        Boolean allowed = null;
        for (SlashPermissionsSlot commandPermission : commandPermissions) {
            if (commandPermission.getType() == SlashPermissionsSlot.Type.CHANNEL) {
                if (commandPermission.isDefaultObject()) {
                    allowed = commandPermission.isAllowed();
                } else {
                    if (commandPermission.getStandardGuildMessageChannelId() == guildMessageChannel.getIdLong()) {
                        return commandPermission.isAllowed();
                    }
                }
            }
        }
        return allowed;
    }

    private static SlashPermissionsSlot.Type mapPermissionType(IntegrationPrivilege.Type type) {
        return switch (type) {
            case ROLE -> SlashPermissionsSlot.Type.ROLE;
            case USER -> SlashPermissionsSlot.Type.USER;
            case CHANNEL -> SlashPermissionsSlot.Type.CHANNEL;
            default -> null;
        };
    }

}
