package commands;

import commands.slashadapters.Slash;
import commands.slashadapters.SlashAdapter;
import commands.slashadapters.SlashMeta;
import core.MainLogger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.reflections.Reflections;

import java.util.*;

public class SlashCommandManager {

    private static final HashMap<String, SlashAdapter> slashAdapterMap = new HashMap<>();
    private static final HashMap<Long, String> idToNameAssociationMap = new HashMap<>();

    static {
        Reflections reflections = new Reflections("commands/slashadapters/adapters");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Slash.class);
        annotated.stream()
                .map(clazz -> {
                    try {
                        return clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        MainLogger.get().error("Error when creating slash adapter class", e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .filter(obj -> obj instanceof SlashAdapter)
                .map(obj -> (SlashAdapter) obj)
                .forEach(SlashCommandManager::insert);
    }

    public static List<CommandData> initialize() {
        ArrayList<CommandData> commandDataList = new ArrayList<>();
        for (SlashAdapter slashAdapter : slashAdapterMap.values()) {
            commandDataList.add(slashAdapter.generateCommandData());
        }
        return commandDataList;
    }


    public static void initialize(List<net.dv8tion.jda.api.interactions.commands.Command> commands) {
        for (net.dv8tion.jda.api.interactions.commands.Command command : commands) {
            idToNameAssociationMap.put(command.getIdLong(), command.getName());
        }
    }

    public static SlashMeta process(SlashCommandInteractionEvent event) {
        SlashAdapter slashAdapter = slashAdapterMap.get(event.getName());
        if (slashAdapter != null) {
            return slashAdapter.process(event);
        } else {
            return null;
        }
    }

    private static void insert(SlashAdapter adapter) {
        slashAdapterMap.put(adapter.name(), adapter);
    }

    public static String getNameFromId(long id) {
        return idToNameAssociationMap.get(id);
    }

    public static String findName(Class<? extends commands.Command> clazz) {
        return commands.Command.getCommandProperties(clazz).trigger();
    }


}
