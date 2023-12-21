package core.assets;

import core.atomicassets.AtomicRole;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;

public interface RoleAsset extends GuildAsset {

    long getRoleId();

    default Optional<Role> getRole() {
        return getGuild().map(guild -> guild.getRoleById(getRoleId()));
    }

    default AtomicRole getAtomicRole() {
        return new AtomicRole(getGuildId(), getRoleId());
    }

}