package commands;

import constants.Language;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public abstract class DMCommand extends Command {

    public DMCommand() {
        super(Language.EN.getLocale(), "S.");
    }

    @Override
    public Locale getLocale() {
        return Language.EN.getLocale();
    }

    @Override
    public Optional<Guild> getGuild() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> getGuildId() {
        return Optional.empty();
    }

    @Override
    public Optional<GuildMessageChannel> getGuildMessageChannel() {
        return Optional.empty();
    }

    @Override
    public Permission[] getUserPermissions() {
        return null;
    }

    @Override
    public Permission[] getAdjustedBotChannelPermissions() {
        return null;
    }

    @Override
    public Permission[] getAdjustedUserChannelPermissions() {
        return null;
    }

    @Override
    public Function<Member, Boolean> getCustomUserGuildPermissions() {
        return null;
    }

    @Override
    public Permission[] getAdjustedBotGuildPermissions() {
        return null;
    }

    @Override
    public Permission[] getAdjustedUserGuildPermissions() {
        return null;
    }

    @Override
    public Optional<Member> getMember() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> getMemberId() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMemberAsMention() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMemberAsTag() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMemberEffectiveAvatarUrl() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMemberEffectiveName() {
        return Optional.empty();
    }

    @Override
    public String getPrefix() {
        return "S.";
    }

}
