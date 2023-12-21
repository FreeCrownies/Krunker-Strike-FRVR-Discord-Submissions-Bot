package mysql.modules.userprivatechannels;

import core.CustomObservableMap;
import mysql.DBDataLoad;
import mysql.DBMain;
import mysql.DBSingleCache;

import java.util.Map;

public class DBUserPrivateChannels extends DBSingleCache<CustomObservableMap<Long, PrivateChannelData>> {

    private static final DBUserPrivateChannels INSTANCE = new DBUserPrivateChannels();

    private DBUserPrivateChannels() {
    }

    public static DBUserPrivateChannels getInstance() {
        return INSTANCE;
    }

    @Override
    protected CustomObservableMap<Long, PrivateChannelData> loadData() throws Exception {
        Map<Long, PrivateChannelData> map = new DBDataLoad<PrivateChannelData>("UserPrivateChannels", "userId, privateChannelId", "1")
                .getMap(
                        PrivateChannelData::getUserId,
                        resultSet -> new PrivateChannelData(
                                resultSet.getLong(1),
                                resultSet.getLong(2)
                        )
                );

        return new CustomObservableMap<>(map)
                .addMapAddListener(this::addPrivateChannel)
                .addMapUpdateListener(this::addPrivateChannel);
    }

    private void addPrivateChannel(PrivateChannelData privateChannelData) {
        DBMain.getInstance().asyncUpdate(
                "REPLACE INTO UserPrivateChannels (userId, privateChannelId) VALUES (?, ?);", preparedStatement -> {
                    preparedStatement.setLong(1, privateChannelData.getUserId());
                    preparedStatement.setLong(2, privateChannelData.getPrivateChannelId());
                }
        );
    }

    @Override
    public Integer getExpirationTimeMinutes() {
        return 1;
    }

}