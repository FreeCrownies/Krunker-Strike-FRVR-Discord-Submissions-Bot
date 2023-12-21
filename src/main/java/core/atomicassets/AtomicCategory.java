package core.atomicassets;

import core.CustomObservableList;
import core.ShardManager;
import mysql.modules.guild.DBGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AtomicCategory implements MentionableAtomicAsset<Category> {

    private final long guildId;
    private final long categoryId;

    public AtomicCategory(long guildId, long categoryId) {
        this.guildId = guildId;
        this.categoryId = categoryId;
    }

    public AtomicCategory(Category category) {
        categoryId = category.getIdLong();
        guildId = category.getGuild().getIdLong();
    }

    public static List<AtomicCategory> from(List<Category> categories) {
        return categories.stream()
                .map(AtomicCategory::new)
                .collect(Collectors.toList());
    }

    public static List<Category> to(List<AtomicCategory> categories) {
        return categories.stream()
                .map(AtomicCategory::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public static CustomObservableList<AtomicCategory> transformIdList(Guild guild, CustomObservableList<Long> list) {
        return list.transform(
                id -> new AtomicCategory(guild.getIdLong(), id),
                AtomicCategory::getIdLong
        );
    }

    @Override
    public long getIdLong() {
        return categoryId;
    }

    @Override
    public Optional<Category> get() {
        return ShardManager.getLocalGuildById(guildId)
                .map(guild -> guild.getCategoryById(categoryId));
    }

    @Override
    public Locale getLocale() {
        return DBGuild.getInstance().retrieve(guildId).getLocale();
    }

    @Override
    public Optional<String> getPrefixedNameRaw() {
        return get().map(c -> "#" + c.getName());
    }

    @Override
    public Optional<String> getNameRaw() {
        return get().map(Category::getName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomicCategory that = (AtomicCategory) o;
        return categoryId == that.categoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

}