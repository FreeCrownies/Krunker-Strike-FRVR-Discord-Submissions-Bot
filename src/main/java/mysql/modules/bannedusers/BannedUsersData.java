package mysql.modules.bannedusers;

import core.CustomObservableList;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Observable;

public class BannedUsersData extends Observable {

    private final CustomObservableList<Long> userIds;

    public BannedUsersData(@NonNull List<Long> userIds) {
        this.userIds = new CustomObservableList<>(userIds);
    }

    public CustomObservableList<Long> getUserIds() {
        return userIds;
    }

}
