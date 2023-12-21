package core.atomicassets;

import core.TextManager;
import net.dv8tion.jda.api.entities.IMentionable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public interface MentionableAtomicAsset<T extends IMentionable> extends IMentionable {

    long getIdLong();

    Optional<T> get();

    Locale getLocale();

    Optional<String> getPrefixedNameRaw();

    default String getPrefixedName() {
        return getPrefixedNameRaw()
                .orElseGet(() -> TextManager.getString(getLocale(), TextManager.GENERAL, "not_found", String.valueOf(getIdLong())));
    }

    Optional<String> getNameRaw();

    default String getName() {
        return getNameRaw()
                .orElseGet(() -> TextManager.getString(getLocale(), TextManager.GENERAL, "not_found", String.valueOf(getIdLong())));
    }

    @Override
    @NotNull
    default String getAsMention() {
        return get()
                .map(IMentionable::getAsMention)
                .orElseGet(() -> "`" + TextManager.getString(getLocale(), TextManager.GENERAL, "not_found", String.valueOf(getIdLong()))+ "`");
    }

}