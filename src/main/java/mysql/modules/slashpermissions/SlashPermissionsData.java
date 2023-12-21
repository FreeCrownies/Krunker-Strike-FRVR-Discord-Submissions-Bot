package mysql.modules.slashpermissions;

import mysql.DataWithGuild;

import java.util.List;
import java.util.Map;

public class SlashPermissionsData extends DataWithGuild {

    private Map<String, List<SlashPermissionsSlot>> permissionMap;

    public SlashPermissionsData(long guildId, Map<String, List<SlashPermissionsSlot>> permissionMap) {
        super(guildId);
        this.permissionMap = permissionMap;
    }

    public Map<String, List<SlashPermissionsSlot>> getPermissionMap() {
        return permissionMap;
    }

    public void setPermissionMap(Map<String, List<SlashPermissionsSlot>> permissionMap) {
        this.permissionMap = permissionMap;
        setChanged();
        notifyObservers();
    }

}
