package events.discordevents.slashcommand;

import commands.Command;
import commands.CommandEvent;
import commands.CommandManager;
import commands.SlashCommandManager;
import commands.slashadapters.SlashMeta;
import constants.Language;
import core.EmbedFactory;
import core.TextManager;
import core.utils.ExceptionUtil;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.SlashCommandAbstract;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Locale;
import java.util.function.Function;

@DiscordEvent
public class SlashCommandCommands extends SlashCommandAbstract {

    @Override
    public boolean onSlashCommand(SlashCommandInteractionEvent event) throws Throwable {
        SlashMeta slashCommandMeta = SlashCommandManager.process(event);
        if (slashCommandMeta == null) {
            EmbedBuilder eb = EmbedFactory.getEmbedError()
                    .setTitle(TextManager.getString(Language.EN.getLocale(), TextManager.GENERAL, "wrong_args"))
                    .setDescription(TextManager.getString(Language.EN.getLocale(), TextManager.GENERAL, "invalid_noargs"));
            event.getHook().sendMessageEmbeds(eb.build())
                    .queue();
            return true;
        }

        String args = slashCommandMeta.getArgs().trim();
        String prefix = "!";
        Locale locale = Language.EN.getLocale();
        Class<? extends Command> clazz = slashCommandMeta.getCommandClass();
        Command command = CommandManager.createCommandByClass(clazz, locale, prefix);
        Function<Locale, String> errorFunction = slashCommandMeta.getErrorFunction();
        if (errorFunction != null) {
            command.getAttachments().put("error", errorFunction.apply(locale));
        }

        try {
            CommandManager.manage(new CommandEvent(event), command, args, getStartTime());
        } catch (Throwable e) {
            ExceptionUtil.handleCommandException(e, command);
        }

        return true;
    }

}