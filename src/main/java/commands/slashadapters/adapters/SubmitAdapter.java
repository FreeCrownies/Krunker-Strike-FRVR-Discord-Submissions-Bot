package commands.slashadapters.adapters;

import commands.runnables.dm.SubmitCommand;
import commands.slashadapters.Slash;
import commands.slashadapters.SlashAdapter;
import commands.slashadapters.SlashMeta;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Slash(command = SubmitCommand.class, description = "Submit your stuff lol", guildOnly = false)
public class SubmitAdapter extends SlashAdapter {

    @Override
    protected SlashCommandData addOptions(SlashCommandData commandData) {
        return commandData;
    }

    @Override
    public SlashMeta process(SlashCommandInteractionEvent event) {
        return new SlashMeta(SubmitCommand.class, "");
    }

}
