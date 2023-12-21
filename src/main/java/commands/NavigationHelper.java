package commands;

import commands.listeners.MessageInputResponse;
import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import core.EmbedFactory;
import core.ListGen;
import core.TextManager;
import core.atomicassets.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.function.Function;

public class NavigationHelper<T> {

    private final NavigationAbstract command;
    private final List<T> srcList;
    private final int max;
    private Type type = Type.Unknown;
    private String typeString = "";
    private Function<T, String> componentNameFunction;
    private Function<T, String> displayNameFunction;

    public NavigationHelper(NavigationAbstract command, List<T> srcList, Class<T> typeClass, int max) {
        this.command = command;
        this.srcList = srcList;
        this.max = max;

        if (typeClass == AtomicRole.class) {
            this.type = Type.Role;
            this.typeString = "_role";
        } else if (typeClass == AtomicTextChannel.class || typeClass == AtomicVoiceChannel.class || typeClass == AtomicStandardGuildMessageChannel.class || typeClass == AtomicGuildMessageChannel.class) {
            this.type = Type.TextChannel;
            this.typeString = "_channel";
        } else if (typeClass == AtomicMember.class) {
            this.type = Type.Member;
            this.typeString = "_user";
        } else if (typeClass == AtomicGuild.class) {
            this.type = Type.Guild;
            this.typeString = "_guild";
        } else if (typeClass == AtomicCategory.class) {
            this.type = Type.Category;
            this.typeString = "_category";
        }
    }

    public MessageInputResponse addData(List<T> newList, String inputString, Member author, int stateBack) {
        if (newList.isEmpty()) {
            command.setLog(LogStatus.FAILURE, TextManager.getNoResultsString(command.getLocale(), inputString));
            return MessageInputResponse.FAILED;
        } else {
            if (type == Type.Role && !command.checkRolesWithLog(author, AtomicRole.to((List<AtomicRole>) newList))) {
                return MessageInputResponse.FAILED;
            }

            int existingElements = 0;
            for (T t : newList) {
                if (srcList.contains(t)) {
                    existingElements++;
                }
            }

            if (existingElements >= newList.size()) {
                command.setLog(LogStatus.FAILURE, TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_exists" + typeString, newList.size() != 1));
                return MessageInputResponse.FAILED;
            }

            int n = 0;
            for (T t : newList) {
                if (!srcList.contains(t)) {
                    if (srcList.size() < max) {
                        srcList.add(t);
                        n++;
                    }
                }
            }

            command.setLog(LogStatus.SUCCESS, TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_add" + typeString, n != 1, String.valueOf(n)));
            command.setState(stateBack);
            return MessageInputResponse.SUCCESS;
        }
    }

    public MessageInputResponse removeData(List<T> removeList, String inputString, int stateBack) {
        if (removeList.isEmpty()) {
            command.setLog(LogStatus.FAILURE, TextManager.getNoResultsString(command.getLocale(), inputString));
            return MessageInputResponse.FAILED;
        } else {

            int notContainedElements = 0;
            for (T t : removeList) {
                if (!srcList.contains(t)) {
                    notContainedElements++;
                }
            }

            if (notContainedElements >= removeList.size()) {
                command.setLog(LogStatus.FAILURE, TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_not_contained" + typeString, removeList.size() != 1));
                return MessageInputResponse.FAILED;
            }

            int n = 0;
            for (T t : removeList) {
                if (srcList.contains(t)) {
                    srcList.remove(t);
                    n++;
                }
            }

            command.setLog(LogStatus.SUCCESS, TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_remove" + typeString, n != 1, String.valueOf(n)));
            command.setState(stateBack);
            return MessageInputResponse.SUCCESS;
        }
    }

    public boolean removeData(int i, int stateBack) {
        if (i == -1) {
            command.setState(stateBack);
            return true;
        } else if (i >= 0 && i < srcList.size()) {
            srcList.remove(i);
            command.setLog(LogStatus.SUCCESS, TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_remove" + typeString, false));
            if (srcList.isEmpty()) command.setState(stateBack);
            return true;
        }

        return false;
    }

    public EmbedBuilder drawDataAdd(boolean allIfEmpty) {
        return drawDataAdd(
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_draw_add_title" + typeString),
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_draw_add_desc" + typeString),
                null,
                allIfEmpty
        );
    }

    public EmbedBuilder drawDataAdd(String title, String name, boolean allIfEmpty) {
        return drawDataAdd(
                title,
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_draw_add_desc" + typeString),
                name,
                allIfEmpty
        );
    }

    public EmbedBuilder drawDataAdd(String title, String desc, String name, boolean allIfEmpty) {
        String valueIfEmpty = TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_draw_empty" + (allIfEmpty ? "_all" : "_none") + typeString);
        EmbedBuilder eb = EmbedFactory.getEmbedDefault(command, desc, title);
        if (getDisplayNameFunction() != null) {
            String listString = new ListGen<T>().getList(srcList, valueIfEmpty, getDisplayNameFunction());
            eb.addField(
                    name == null ? command.getString("state0_name") : name,
                    listString,
                    true
            );
        }
        return eb;
    }

    public EmbedBuilder drawDataRemove(boolean messageInput) {
        return drawDataRemove(
                messageInput,
                null
        );
    }

    public EmbedBuilder drawDataRemove(boolean messageInput, String name) {
        String typeMessageInput = messageInput ? "_messageinput" : "";
        return drawDataRemove(
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_draw_remove_title" + typeString),
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_draw_remove_desc" + typeMessageInput + typeString),
                name
        );
    }

    public EmbedBuilder drawDataRemove(String title, String name) {
        return drawDataRemove(
                title,
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_draw_remove_desc" + typeString),
                name
        );
    }

    public EmbedBuilder drawDataRemove(String title, String desc, String name) {
        String[] strings = new String[srcList.size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = getComponentNameFunction().apply(srcList.get(i));
        }
        command.setComponents(strings);
        EmbedBuilder eb = EmbedFactory.getEmbedDefault(command, desc, title);
        if (getDisplayNameFunction() != null) {
            String listString = new ListGen<T>().getList(srcList, getDisplayNameFunction());
            eb.addField(
                    name == null ? command.getString("state0_name") : name,
                    listString,
                    true
            );
        }
        return eb;
    }

    public void startDataAdd(int stateNext) {
        if (srcList.size() < max) {
            command.setState(stateNext);
        } else {
            command.setLog(LogStatus.FAILURE, TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_start_add_toomany" + typeString, String.valueOf(max)));
        }
    }

    public void startDataRemove(int stateNext) {
        if (srcList.size() > 0) {
            command.setState(stateNext);
        } else {
            command.setLog(LogStatus.FAILURE, TextManager.getString(command.getLocale(), TextManager.GENERAL, "element_start_remove_none" + typeString, String.valueOf(max)));
        }
    }

    public int size() {
        return srcList.size();
    }

    public List<T> getSrcList() {
        return srcList;
    }

    private Function<T, String> getDisplayNameFunction() {
        if (displayNameFunction != null) {
            return displayNameFunction;
        }

        if (type == Type.Unknown) return null;

        if (type.equals(Type.Guild)) {
            displayNameFunction = obj -> ((AtomicGuild) obj).getName();
        } else {
            displayNameFunction = obj -> ((IMentionable) obj).getAsMention();
        }
        return displayNameFunction;
    }

    public NavigationHelper<T> setDisplayNameFunction(Function<T, String> displayNameFunction) {
        this.displayNameFunction = displayNameFunction;
        return this;
    }

    private Function<T, String> getComponentNameFunction() {
        if (componentNameFunction != null) {
            return componentNameFunction;
        }

        if (type == Type.Unknown) {
            componentNameFunction = Object::toString;
        } else if (type == Type.Guild) {
            componentNameFunction = obj -> ((AtomicGuild) obj).getName();
        } else {
            componentNameFunction = obj -> ((MentionableAtomicAsset<?>) obj).getPrefixedName();
        }
        return componentNameFunction;
    }

    public NavigationHelper<T> setComponentNameFunction(Function<T, String> componentNameFunction) {
        this.componentNameFunction = componentNameFunction;
        return this;
    }

    private enum Type {Unknown, Role, TextChannel, Member, Guild, Category}

}