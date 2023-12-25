package commands;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import commands.listeners.OnStaticReactionAddListener;
import commands.listeners.OnStaticReactionRemoveListener;
import commands.runnables.dm.SubmitCommand;
import constants.Settings;
import core.MainLogger;
import core.utils.ExceptionUtil;

import java.time.Duration;
import java.util.*;

public class CommandContainer {

    private static final HashMap<String, Class<? extends Command>> commandMap = new HashMap<>();
    private static final HashMap<Category, ArrayList<Class<? extends Command>>> commandCategoryMap = new HashMap<>();
    private static final ArrayList<Class<? extends OnStaticReactionAddListener>> staticReactionAddCommands = new ArrayList<>();
    private static final ArrayList<Class<? extends OnStaticReactionRemoveListener>> staticReactionRemoveCommands = new ArrayList<>();

    private static final HashMap<Class<?>, Cache<Long, CommandListenerMeta<?>>> listenerMap = new HashMap<>();

    private static int commandStuckCounter = 0;

    static {
        final ArrayList<Class<? extends Command>> commandList = new ArrayList<>();


        commandList.add(SubmitCommand.class);


        for (Class<? extends Command> clazz : commandList) {
            Command command = CommandManager.createCommandByClass(clazz, Locale.US, "S.");
            addCommand(command.getTrigger(), command);

            for (String str : command.getCommandProperties().aliases()) addCommand(str, command);

            if (command instanceof OnStaticReactionAddListener) {
                staticReactionAddCommands.add(((OnStaticReactionAddListener) command).getClass());
            }
            if (command instanceof OnStaticReactionRemoveListener) {
                staticReactionRemoveCommands.add(((OnStaticReactionRemoveListener) command).getClass());
            }

            if (command.canRunOnGuild(0L, 0L)) {
                addCommandCategoryMap(command);
            }
        }
    }

    private static void addCommandCategoryMap(Command command) {
        ArrayList<Class<? extends Command>> commands = commandCategoryMap.computeIfAbsent(command.getCategory(), e -> new ArrayList<>());
        commands.add(command.getClass());
    }

    private static void addCommand(String trigger, Command command) {
        if (commandMap.containsKey(trigger)) {
            MainLogger.get().error("Duplicate key for command \"" + command.getTrigger() + "\"");
        } else {
            commandMap.put(trigger, command.getClass());
        }
    }

    public static HashMap<String, Class<? extends Command>> getCommandMap() {
        return commandMap;
    }

    public static ArrayList<Class<? extends OnStaticReactionAddListener>> getStaticReactionAddCommands() {
        return staticReactionAddCommands;
    }

    public static ArrayList<Class<? extends OnStaticReactionRemoveListener>> getStaticReactionRemoveCommands() {
        return staticReactionRemoveCommands;
    }

    public static HashMap<Category, ArrayList<Class<? extends Command>>> getCommandCategoryMap() {
        return commandCategoryMap;
    }

    public static ArrayList<Class<? extends Command>> getFullCommandList() {
        ArrayList<Class<? extends Command>> fullList = new ArrayList<>();
        getCommandCategoryMap().values()
                .forEach(fullList::addAll);

        return fullList;
    }

    public static synchronized <T> void registerListener(Class<?> clazz, CommandListenerMeta<T> commandListenerMeta) {
        Cache<Long, CommandListenerMeta<?>> cache = listenerMap.computeIfAbsent(
                clazz,
                e -> CacheBuilder.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(Settings.TIME_OUT_MINUTES))
                        .removalListener(event -> {
                            if (event.getCause() == RemovalCause.EXPIRED) {
                                ((CommandListenerMeta<?>) event.getValue()).timeOut();
                            }
                        })
                        .build()
        );
        cache.put(commandListenerMeta.getCommand().getId(), commandListenerMeta);
    }

    public static synchronized void deregisterListeners(Command command) {
        for (Cache<Long, CommandListenerMeta<?>> cache : listenerMap.values()) {
            cache.invalidate(command.getId());
        }
    }

    public static synchronized Collection<CommandListenerMeta<?>> getListeners(Class<?> clazz) {
        if (!listenerMap.containsKey(clazz)) {
            return Collections.emptyList();
        }
        return listenerMap.get(clazz).asMap().values();
    }

    public static synchronized Optional<CommandListenerMeta<?>> getListener(Class<?> clazz, Command command) {
        if (!listenerMap.containsKey(clazz)) {
            return Optional.empty();
        }
        return Optional.ofNullable(listenerMap.get(clazz).getIfPresent(command.getId()));
    }

    public static synchronized void cleanUp() {
        listenerMap.values().forEach(Cache::cleanUp);
    }

    public static synchronized void refreshListeners(Command command) {
        for (Cache<Long, CommandListenerMeta<?>> cache : listenerMap.values()) {
            CommandListenerMeta<?> meta = cache.getIfPresent(command.getId());
            if (meta != null) {
                cache.put(command.getId(), meta);
            }
        }
    }

    public static synchronized Collection<Class<?>> getListenerClasses() {
        return listenerMap.keySet();
    }

    public static synchronized int getListenerSize() {
        return (int) listenerMap.values().stream()
                .mapToLong(Cache::size)
                .sum();
    }

    public static void addCommandTerminationStatus(Command command, Thread commandThread, boolean stuck) {
        if (stuck) {
            Exception e = ExceptionUtil.generateForStack(commandThread);
            MainLogger.get().error("Command \"{}\" stuck (stuck counter: {})", command.getTrigger(), ++commandStuckCounter, e);
            commandThread.interrupt();
        } else {
            commandStuckCounter = Math.max(0, commandStuckCounter - 1);
        }
    }

    public static int getCommandStuckCounter() {
        return commandStuckCounter;
    }

}