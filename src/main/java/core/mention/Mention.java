package core.mention;

import net.dv8tion.jda.api.entities.ISnowflake;

import java.util.List;
import java.util.Optional;

public class Mention {

    private final String filteredArgs;
    private final String mentionText;
    private final String mentionTextWithIds;
    private final boolean multiple;
    private final boolean containedBlockedUser;
    private final List<ISnowflake> elementList;

    public Mention(String mentionText, String mentionTextWithIds, String filteredArgs, boolean multiple, boolean containedBlockedUser,
                   List<ISnowflake> elementList
    ) {
        this.mentionText = mentionText;
        this.mentionTextWithIds = mentionTextWithIds;
        this.filteredArgs = filteredArgs;
        this.multiple = multiple;
        this.containedBlockedUser = containedBlockedUser;
        this.elementList = elementList;
    }

    public String getMentionText() {
        return mentionText;
    }

    public String getMentionTextWithIds() {
        return mentionTextWithIds;
    }

    public Optional<String> getFilteredArgs() {
        return Optional.ofNullable(filteredArgs).map(String::trim);
    }

    public boolean isMultiple() {
        return multiple;
    }

    public boolean containedBlockedUser() {
        return containedBlockedUser;
    }

    public List<ISnowflake> getElementList() {
        return elementList;
    }

}