package mysql.modules.slashpermissions;

import core.MainLogger;
import mysql.DBDataLoad;
import mysql.DBMain;
import mysql.DBObserverMapCache;

import java.sql.SQLException;
import java.util.*;

public class DBSlashPermissions extends DBObserverMapCache<Long, SlashPermissionsData> {

    private static final DBSlashPermissions ourInstance = new DBSlashPermissions();

    public static DBSlashPermissions getInstance() {
        return ourInstance;
    }

    private DBSlashPermissions() {
    }

    @Override
    protected SlashPermissionsData load(Long serverId) throws Exception {
        HashMap<String, List<SlashPermissionsSlot>> permissionMap = new HashMap<>();
        List<SlashPermissionsSlot> permissionList = new DBDataLoad<SlashPermissionsSlot>("SlashPermissions", "command, object_id, object_type, allowed", "guild_id = ?",
                preparedStatement -> preparedStatement.setLong(1, serverId)
        ).getList(resultSet -> new SlashPermissionsSlot(
                        serverId,
                        resultSet.getString(1),
                        resultSet.getLong(2),
                        SlashPermissionsSlot.Type.values()[resultSet.getInt(3)],
                        resultSet.getBoolean(4)
                )
        );
        for (SlashPermissionsSlot slashPermissionsSlot : permissionList) {
            ArrayList<SlashPermissionsSlot> commandPermissionList = (ArrayList<SlashPermissionsSlot>) permissionMap
                    .computeIfAbsent(slashPermissionsSlot.getCommand(), k -> new ArrayList<>());
            commandPermissionList.add(slashPermissionsSlot);
        }

        return new SlashPermissionsData(
                serverId,
                Collections.unmodifiableMap(permissionMap)
        );
    }

    @Override
    protected void save(SlashPermissionsData slashPermissionsData) {
        try {
            DBMain.getInstance().update("DELETE FROM SlashPermissions WHERE guild_id = ?;", preparedStatement -> {
                preparedStatement.setLong(1, slashPermissionsData.getGuildId());
            });
        } catch (SQLException | InterruptedException e) {
            MainLogger.get().error("Error while deleting SlashPermissions for guild " + slashPermissionsData.getGuildId(), e);
        }

        Map<String, List<SlashPermissionsSlot>> permissionMap = slashPermissionsData.getPermissionMap();
        for (String command : permissionMap.keySet()) {
            for (SlashPermissionsSlot slot : permissionMap.get(command)) {
                System.out.println("updating... (object id: " + slot.getObjectId() + ")");
                DBMain.getInstance().asyncUpdate("INSERT INTO SlashPermissions (guild_id, command, object_id, object_type, allowed) VALUES (?, ?, ?, ?, ?);", preparedStatement -> {
                    preparedStatement.setLong(1, slot.getGuildId());
                    preparedStatement.setString(2, slot.getCommand());
                    preparedStatement.setLong(3, slot.getObjectId());
                    preparedStatement.setInt(4, slot.getType().ordinal());
                    preparedStatement.setBoolean(5, slot.isAllowed());
                });
            }
        }
    }

}
