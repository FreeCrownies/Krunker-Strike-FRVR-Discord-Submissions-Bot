package core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import core.utils.StringUtil;

import java.time.OffsetDateTime;
import java.util.Iterator;

public class DiscordConsole extends AppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent> {

    private static final WebhookClient webhookClient = new WebhookClientBuilder(1187396711027585044L, "PEBUC3_wCeb3KrVMJfny_hsdByfXIskvOqzJcLDpFp_S8FkSijuXB9K-MpuXeGQx3bMx")
            .build();
    private final AppenderAttachableImpl<ILoggingEvent> appenderAttachable = new AppenderAttachableImpl<>();

    public DiscordConsole() {
    }

//    public static void setup() {
//        DiscordConsole interceptor = new DiscordConsole();
//        interceptor.setName("DiscordConsole");
//        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
//        logger.addAppender(interceptor);
//    }

    public static void sendConsoleMessage(WebhookMessage webhookMessage) {
        webhookClient.send(webhookMessage).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!Program.productionMode()) return;

        WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();

        StringBuilder description = new StringBuilder()
                .append("```md\n")
                .append(eventObject.getFormattedMessage())
                .append("```");

        if (eventObject.getLevel() == Level.ERROR) {
            description.append("\n```java\n")
                    .append(eventObject.getThrowableProxy().getClassName())
                    .append(": ")
                    .append(eventObject.getThrowableProxy().getMessage())
                    .append("\n");
            for (StackTraceElementProxy element : eventObject.getThrowableProxy().getStackTraceElementProxyArray()) {
                description.append(element.getStackTraceElement().toString()).append("\n");
            }
            StringUtil.shortenString(description.toString(), 2045);
            description.append("```");

            messageBuilder.setContent("@everyone");
        }

        if (eventObject.getLevel() == Level.WARN) {
            description.append("\n```java\n")
                    .append(eventObject.getThrowableProxy().getClassName())
                    .append(": ")
                    .append(eventObject.getThrowableProxy().getMessage())
                    .append("\n");
            for (StackTraceElementProxy element : eventObject.getThrowableProxy().getStackTraceElementProxyArray()) {
                description.append(element.getStackTraceElement().toString()).append("\n");
            }
            StringUtil.shortenString(description.toString(), 2045);
            description.append("```");
        }

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle(eventObject.getThreadName(), null))
                .setColor(eventObject.getLevel() == Level.ERROR ? 0xFF0000 : eventObject.getLevel() == Level.WARN ? 0xFFFFF00 : 0xFFFFFF)
                .setDescription(description.toString())
                .setTimestamp(OffsetDateTime.now())
                .build();

        messageBuilder.addEmbeds(embed);

        sendConsoleMessage(messageBuilder.build());
//        appenderAttachable.appendLoopOnAppenders(eventObject);
    }

    @Override
    public void addAppender(Appender<ILoggingEvent> newAppender) {
        appenderAttachable.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        return appenderAttachable.iteratorForAppenders();
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        return appenderAttachable.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return appenderAttachable.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        appenderAttachable.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        return appenderAttachable.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return appenderAttachable.detachAppender(name);
    }

}
