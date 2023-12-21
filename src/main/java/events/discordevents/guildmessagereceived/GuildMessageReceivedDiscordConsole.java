package events.discordevents.guildmessagereceived;

import constants.AssetIds;
import core.Console;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.GuildMessageReceivedAbstract;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

@DiscordEvent
public class GuildMessageReceivedDiscordConsole extends GuildMessageReceivedAbstract {

    private static final long[] ALLOWED_USERS = new long[] {
            AssetIds.OWNER_USER_ID,
            1027496739697082388L,
            395748060283535386L
    };

    @Override
    public boolean onGuildMessageReceived(MessageReceivedEvent event) throws Throwable {
        if (event.getChannel().getIdLong() != 1187396625501520072L) {
            return true;
        }
        if (Arrays.stream(ALLOWED_USERS).noneMatch(id -> id == event.getAuthor().getIdLong())) {
            return true;
        }

        Console.processInput(event.getMessage().getContentRaw(), Console.ConsoleType.DISCORD);
        return false;
    }

}