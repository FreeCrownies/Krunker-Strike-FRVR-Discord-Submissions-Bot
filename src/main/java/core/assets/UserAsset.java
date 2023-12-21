package core.assets;

import core.ShardManager;
import net.dv8tion.jda.api.entities.User;

import java.util.Optional;

public interface UserAsset {

    long getUserId();

    default Optional<User> getUser() {
        return ShardManager.getCachedUserById(getUserId());
    }

}