package commands.slashadapters;

import commands.Category;
import commands.Command;
import commands.CommandContainer;
import constants.Language;
import core.TextManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Arrays;

public abstract class SlashAdapter {

    protected static String collectArgs(SlashCommandInteractionEvent event, String... exceptions) {
        StringBuilder argsBuilder = new StringBuilder();
        for (OptionMapping option : event.getOptions()) {
            if (Arrays.stream(exceptions).noneMatch(exception -> option.getName().equals(exception))) {
                if (option.getType() == OptionType.BOOLEAN && option.getAsBoolean()) {
                    argsBuilder.append(option.getName()).append(" ");
                } else {
                    argsBuilder.append(option.getAsString()).append(" ");
                }
            }
        }

        return argsBuilder.toString();
    }

    protected abstract SlashCommandData addOptions(SlashCommandData commandData);

    public abstract SlashMeta process(SlashCommandInteractionEvent event);

    public String name() {
        Slash slash = getClass().getAnnotation(Slash.class);
        String name = slash.name();
        if (name.isEmpty()) {
            name = Command.getCommandProperties(slash.command()).trigger();
        }
        return name;
    }

    public String description() {
        Slash slash = getClass().getAnnotation(Slash.class);
        String description = slash.description();
        if (description.isEmpty()) {
            String trigger = name();
            Class<? extends Command> clazz = CommandContainer.getCommandMap().get(trigger);
            Category category = Command.getCategory(clazz);
            description = TextManager.getString(Language.EN.getLocale(), category, trigger + "_description");
        }
        return description;
    }

    public Class<? extends Command> commandClass() {
        Slash slash = getClass().getAnnotation(Slash.class);
        return slash.command();
    }

    public SlashCommandData generateCommandData() {
        Slash slash = getClass().getAnnotation(Slash.class);
        SlashCommandData commandData = Commands.slash(name(), description());
        commandData.setGuildOnly(slash.guildOnly());
        return addOptions(commandData);
    }

}