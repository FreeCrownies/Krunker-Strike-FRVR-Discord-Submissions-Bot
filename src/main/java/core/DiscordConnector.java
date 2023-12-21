package core;

import commands.SlashCommandManager;
import events.discordevents.DiscordEventAdapter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ConcurrentSessionController;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;

import java.util.EnumSet;
import java.util.List;

public class DiscordConnector {

    private static final ConcurrentSessionController concurrentSessionController = new ConcurrentSessionController();
    private static final JDABuilder jdaBuilder = JDABuilder.createDefault(System.getenv("BOT_TOKEN"))
            .setSessionController(concurrentSessionController)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setChunkingFilter(ChunkingFilterController.getInstance())
            .enableIntents(
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_MESSAGE_TYPING
            )
            .disableCache(CacheFlag.ROLE_TAGS, CacheFlag.ACTIVITY)
            .setActivity(Activity.watching("your submissions"))
            .addEventListeners(new DiscordEventAdapter());
    private static boolean started = false;

    static {
        concurrentSessionController.setConcurrency(1);
        ShardManager.addShardDisconnectConsumer(DiscordConnector::reconnectApi);
    }

    public static void connect(int shardMin, int shardMax, int totalShards) {
        if (started) return;
        started = true;

        MainLogger.get().info("Bot is logging in...");
        ShardManager.init(shardMin, shardMax, totalShards);
        EnumSet<Message.MentionType> deny = EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE, Message.MentionType.USER);
        MessageRequest.setDefaultMentions(EnumSet.complementOf(deny));
        MessageRequest.setDefaultMentionRepliedUser(false);

        new Thread(() -> {
            for (int i = shardMin; i <= shardMax; i++) {
                try {
                    jdaBuilder.useSharding(i, totalShards)
                            .build();
                } catch (InvalidTokenException e) {
                    MainLogger.get().error("EXIT - Invalid token", e);
                    System.exit(2);
                }
            }
        }, "Shard-Starter").start();
    }

    public static void reconnectApi(int shardId) {
        MainLogger.get().info("Shard {} is getting reconnected...", shardId);

        try {
            jdaBuilder.useSharding(shardId, ShardManager.getTotalShards())
                    .build();
        } catch (InvalidTokenException e) {
            MainLogger.get().error("EXIT - Invalid token", e);
            System.exit(3);
        }
    }

    public static void onJDAJoin(JDA jda) {
        ShardManager.addJDA(jda);
        MainLogger.get().info("Shard {} connection established", jda.getShardInfo().getShardId());

        checkConnectionCompleted();
    }

    private synchronized static void checkConnectionCompleted() {
        if (ShardManager.isEverythingConnected() && !ShardManager.isReady()) {
            onConnectionCompleted();
        }
    }

    private synchronized static void onConnectionCompleted() {
        MainLogger.get().info("### ALL SHARDS CONNECTED SUCCESSFULLY! ###");

        ShardManager.start();

        try {
            loadSlashCommands();
        } catch (Throwable e) {
            MainLogger.get().error("Exception on slash commands load", e);
        }
    }

    public static boolean isStarted() {
        return started;
    }

    public static void disconnect() {
        ShardManager.getConnectedLocalJDAs().forEach(JDA::shutdownNow);
    }

    public static void loadSlashCommands() {
        List<CommandData> commandDataList = SlashCommandManager.initialize();
        ShardManager.getAnyJDA().get()
                .updateCommands()
                .addCommands(commandDataList)
                .queue(SlashCommandManager::initialize);
    }

}