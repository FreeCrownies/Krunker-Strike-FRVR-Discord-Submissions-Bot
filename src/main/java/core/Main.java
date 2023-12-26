package core;

import mysql.DBMain;

import java.util.TimeZone;

public class Main {

    public static void main(String[] args) {
        try {
            Program.init();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Console.start();
            DBMain.getInstance().connect();

            DiscordConnector.connect(0, 0, 1);
            if (Program.productionMode()) {
                Runtime.getRuntime().addShutdownHook(new Thread(Program::onStop, "Shutdown Bot-Stop"));
            }
        } catch (Throwable e) {
            MainLogger.get().error("EXIT - Error on startup", e);
            System.exit(4);
        }
    }

}
