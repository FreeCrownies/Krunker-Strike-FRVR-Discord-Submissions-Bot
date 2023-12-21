package core;

import java.time.Instant;

public class Program {

    private final static Instant startTime = Instant.now();
    private static boolean stopped = false;
    private static boolean newVersion = false;

    public static void init() {
        MainLogger.get().info(
                "\n" +
                        "-------------------------------------\n" +
                        "Production Mode: " + productionMode() + "\n" +
//                        "Version: " + **** + "\n" +
                        "-------------------------------------"
        );
    }

    public static void onStop() {
        MainLogger.get().info(Console.getMemory());
        MainLogger.get().info("### STOPPING BOT ###");
        stopped = true;
        ShardManager.stop();
    }

    public static boolean productionMode() {
        return true;
    }


    public static boolean isRunning() {
        return !stopped;
    }

    public static boolean publicVersion() {
        return true;
    }

    public static Instant getStartTime() {
        return startTime;
    }

    public static boolean isNewVersion() {
        return newVersion;
    }

    public static void setNewVersion() {
        Program.newVersion = true;
    }


}
