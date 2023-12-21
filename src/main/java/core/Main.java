package core;

import java.util.TimeZone;

public class Main {

    public static void main(String[] args) {
        try {
            Program.init();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Console.start();
//            DBMain.getInstance().connect();

            DiscordConnector.connect(0, Program.productionMode() ? 1 : 0, Program.productionMode() ? 2 : 1);
            if (Program.productionMode()) {
                Runtime.getRuntime().addShutdownHook(new Thread(Program::onStop, "Shutdown Bot-Stop"));
            }
//            Updater.onUpdate();
        } catch (Throwable e) {
            MainLogger.get().error("EXIT - Error on startup", e);
            System.exit(4);
        }
    }

}
