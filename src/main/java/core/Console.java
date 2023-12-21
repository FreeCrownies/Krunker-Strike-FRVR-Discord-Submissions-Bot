package core;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import commands.CommandContainer;
import commands.slashadapters.runningchecker.RunningCheckerManager;
import core.utils.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Console {

    private static final HashMap<String, ConsoleTask> tasks = new HashMap<>();
    private static final Cache<Long, ConsoleType> consoleTypeCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    static {
        registerTasks();
    }

    public static void start() {
        Thread t = new Thread(Console::manageConsole, "Console");
        t.setDaemon(true);
        t.start();
    }

    private static void registerTasks() {
        tasks.put("help", Console::onHelp);

        tasks.put("quit", Console::onQuit);
        tasks.put("reconnect", Console::onReconnect);
        tasks.put("threads", Console::onThreads);
        tasks.put("threads_interrupt", Console::onThreadsInterrupt);
        tasks.put("reload_slash_commands", Console::onReloadSlashCommands);
        tasks.put("disconnect", Console::onDisconnect);
    }

    private static void onReloadSlashCommands(long inputId, String[] args) {
        DiscordConnector.loadSlashCommands();
        print(inputId, "Successfully loaded slash commands");
    }

    private static void onThreadsInterrupt(long inputId, String[] args) {
        int stopped = 0;

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (args.length < 2 || t.getName().matches(args[1])) {
                t.interrupt();
                stopped++;
            }
        }

        print(inputId, "{} thread/s interrupted", stopped);
    }

    private static void onThreads(long inputId, String[] args) {
        StringBuilder sb = new StringBuilder();

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (args.length < 2 || t.getName().matches(args[1])) {
                sb.append(t.getName()).append(", ");
            }
        }

        String str = sb.toString();
        if (str.length() >= 2) str = str.substring(0, str.length() - 2);

        print(inputId, "\n--- THREADS ({}) ---\n{}\n", Thread.getAllStackTraces().size(), str);
    }

    private static void onReconnect(long inputId, String[] args) {
        int shardId = Integer.parseInt(args[1]);
        ShardManager.reconnectShard(shardId);
        print(inputId, "Reconnecting shard {}", shardId);
    }

    private static void onQuit(long inputId, String[] args) {
        print(inputId, "EXIT - Stopping Program");
        System.exit(0);
    }

    private static void onDisconnect(long inputId, String[] args) {
        print(inputId, "Disconnecting...");
        DiscordConnector.disconnect();
        // DBMain.getInstance().disconnect();
        System.exit(0);
    }

    private static void onHelp(long inputId, String[] args) {
        StringBuilder help = new StringBuilder();
        tasks.keySet().stream()
                .filter(key -> !key.equals("help"))
                .sorted()
                .forEach(key -> help.append("- ").append(key).append("\n"));
        print(inputId, StringUtil.replaceLast(help.toString(), "\n", ""));
    }


    private static void manageConsole() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.length() > 0) {
                    processInput(line, ConsoleType.CONSOLE);
                }
            }
        }
    }

    public static void processInput(String input, ConsoleType consoleType) {
        long inputId = System.nanoTime();
        consoleTypeCache.put(inputId, consoleType);

        String[] args = input.split(" ");
        ConsoleTask task = tasks.get(args[0]);
        if (task != null) {
            GlobalThreadPool.getExecutorService().submit(() -> {
                try {
                    task.process(inputId, args);
                } catch (Throwable throwable) {
                    print(inputId, "Console task {} ended with exception\n{}: {}", args[0], throwable.getMessage(), Arrays.toString(throwable.getStackTrace()));
                }
            });
        } else {
            print(inputId, "No result for '{}'", args[0]);
        }
    }

    public static String getMemory() {
        StringBuilder sb = new StringBuilder();
        double memoryTotal = Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0);
        double memoryUsed = memoryTotal - (Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0));
        sb.append("Memory: ")
                .append(String.format("%1$.2f", memoryUsed))
                .append(" / ")
                .append(String.format("%1$.2f", memoryTotal))
                .append(" MB");

        return sb.toString();
    }

    public static String getStats() {
        String header = "--- STATS Program ---";
        StringBuilder sb = new StringBuilder("\n" + header + "\n");

        // heap memory
        double memoryTotal = Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0);
        double memoryUsed = memoryTotal - (Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0));
        sb.append("Heap Memory Memory: ")
                .append(String.format("%1$.2f", memoryUsed))
                .append(" / ")
                .append(String.format("%1$.2f", memoryTotal))
                .append(" MB\n");

        // threads
        sb.append("Threads: ")
                .append(Thread.getAllStackTraces().keySet().size())
                .append("\n");

        // active listeners
        sb.append("Active Listeners: ")
                .append(CommandContainer.getListenerSize()).append(" Commands | ")
                .append("\n");

        // running commands
        sb.append("Running Commands: ")
                .append(RunningCheckerManager.getRunningCommandsMap().size())
                .append("\n");

        sb.append("-".repeat(header.length()))
                .append("\n");
        return sb.toString();
    }

    private static void print(long inputId, String message, Object... args) {
        message = String.format(message.replace("{}", "%s"), args) + "\n";
        if (consoleTypeCache.asMap().get(inputId) == ConsoleType.CONSOLE) {
            System.out.printf(message);
        } else {
            WebhookMessageBuilder builder = new WebhookMessageBuilder()
                    .append(StringUtil.shortenString(message, 4096));
            DiscordConsole.sendConsoleMessage(builder.build());
        }
    }

    public enum ConsoleType {
        DISCORD, CONSOLE
    }

    public interface ConsoleTask {

        void process(long inputId, String[] args) throws Throwable;

    }

}