package commands;

import commands.cooldownchecker.CoolDownManager;
import commands.cooldownchecker.CoolDownUserData;
import commands.listeners.OnButtonListener;
import commands.listeners.OnMessageInputListener;
import commands.listeners.OnReactionListener;
import commands.listeners.OnStringSelectMenuListener;
import commands.runnables.dm.SubmitCommand;
import commands.slashadapters.runningchecker.RunningCheckerManager;
import constants.Emojis;
import constants.Settings;
import core.*;
import core.components.ActionRows;
import core.schedule.MainScheduler;
import core.utils.BotPermissionUtil;
import core.utils.ExceptionUtil;
import core.utils.JDAUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager {

    private final static int SEC_UNTIL_REMOVAL = 20;
    private final static Random random = new Random();

    public static void manage(CommandEvent event, Command command, String args, Instant startTime) {
        manage(event, command, args, startTime, true);
    }

    public static void manage(CommandEvent event, Command command, String args, Instant startTime, boolean freshCommand) {
        if (checkCoolDown(event, command) &&
                checkCorrectChannelType(event, command) &&
                checkRunningCommands(event, command)
        ) {
            process(event, command, args, startTime, freshCommand);
        }

        command.getCompletedListeners().forEach(runnable -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                MainLogger.get().error("Error on completed listener", e);
            }
        });
    }

    private static void process(CommandEvent event, Command command, String args, Instant startTime, boolean freshCommand) {
        if (command instanceof DMCommand ||
                command instanceof SubmitCommand ||
                (botCanPost(event, command) &&
                        botCanUseEmbeds(event, command) &&
                        canRunOnGuild(event, command) &&
                        checkTurnedOn(event, command) &&
                        checkPermissions(event, command)
                )
        ) {
            try {
                cleanPreviousListeners(command, event.getUser());
                sendOverwrittenSignals(command, event.getUser());

                boolean success = command.processTrigger(event, args, freshCommand);
                if (success && Program.publicVersion()) {
                    // maybeSendBotInvite(event, command.getLocale());
                }
            } catch (Throwable e) {
                ExceptionUtil.handleCommandException(e, command);
            } finally {
                CommandContainer.cleanUp();
            }
        }
    }

    private static boolean checkRunningCommands(CommandEvent event, Command command) {
        // To-do add custom checker for dm commands
        if (command instanceof SubmitCommand) return true;
        if (RunningCheckerManager.canUserRunCommand(
                command,
                event.getGuild().getIdLong(),
                event.getMember().getIdLong(),
                event.getJDA().getShardInfo().getShardId(),
                command.getCommandProperties().maxCalculationTimeSec()
        )) {
            return true;
        }

        if (CoolDownManager.getCoolDownData(event.getMember().getIdLong()).canPostCoolDownMessage()) {
            String desc = TextManager.getString(command.getLocale(), TextManager.GENERAL, "already_used_desc");

            if (BotPermissionUtil.canWriteEmbed(event.getGuildMessageChannel()) || event.isSlashCommandInteractionEvent()) {
                EmbedBuilder eb = EmbedFactory.getEmbedError()
                        .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "already_used_title"))
                        .setDescription(desc);
                sendError(event, command.getLocale(), eb, true);
            } else if (BotPermissionUtil.canWrite(event.getGuildMessageChannel())) {
                sendErrorNoEmbed(event, command.getLocale(), desc, true);
            }
        }
        return false;
    }

    private static boolean checkCorrectChannelType(CommandEvent event, Command command) {
        return true;
//        if (event.getChannel() instanceof TextChannel) {
//            return true;
//        }
//
//        String desc = TextManager.getString(command.getLocale(), TextManager.GENERAL, "wrong_channel_type_desc");
//        if (BotPermissionUtil.canWriteEmbed(event.getGuildMessageChannel()) || event.isSlashCommandInteractionEvent()) {
//            EmbedBuilder eb = EmbedFactory.getEmbedError()
//                    .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "wrong_channel_type_title"))
//                    .setDescription(desc);
//            sendError(event, command.getLocale(), eb, true);
//        } else if (BotPermissionUtil.canWrite(event.getGuildMessageChannel())) {
//            sendErrorNoEmbed(event, command.getLocale(), desc, true);
//        }
//
//        return false;
    }


    private static boolean checkCoolDown(CommandEvent event, Command command) {
        // To-do: Add custom cooldown for dm commands
        if (command instanceof DMCommand) {
            return true;
        }

        CoolDownUserData cooldownUserData = CoolDownManager.getCoolDownData(event.getUser().getIdLong());

        Optional<Integer> waitingSec = cooldownUserData.getWaitingSec(Settings.COOLDOWN_TIME_SEC);
        if (waitingSec.isEmpty()) {
            return true;
        }

        if (cooldownUserData.canPostCoolDownMessage()) {
            String desc = TextManager.getString(command.getLocale(), TextManager.GENERAL, "cooldown_description", waitingSec.get() != 1, String.valueOf(waitingSec.get()));

            if (BotPermissionUtil.canWriteEmbed(event.getGuildMessageChannel()) || event.isSlashCommandInteractionEvent()) {
                EmbedBuilder eb = EmbedFactory.getEmbedError()
                        .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "cooldown_title"))
                        .setDescription(desc);
                sendError(event, command.getLocale(), eb, false);
            } else if (BotPermissionUtil.canWrite(event.getGuildMessageChannel())) {
                sendErrorNoEmbed(event, command.getLocale(), desc, false);
            }
        }

        return false;
    }

    private static boolean checkPermissions(CommandEvent event, Command command) {
        Boolean customPermissions = checkCommandPermissions(event, command);

        if (customPermissions != null) {
            return customPermissions;
        }

        Permission[] botChannelPermissions = command.getAdjustedBotChannelPermissions();

        EmbedBuilder errEmbed = BotPermissionUtil.getUserAndBotPermissionMissingEmbed(
                command.getLocale(),
                event.getGuildMessageChannel(),
                event.getMember(),
                command.getAdjustedUserGuildPermissions(),
                command.getCustomUserGuildPermissions(),
                command.getAdjustedUserChannelPermissions(),
                command.getAdjustedBotGuildPermissions(),
                botChannelPermissions
        );
        if (errEmbed == null) {
            return true;
        }

        sendError(event, command.getLocale(), errEmbed, true);
        return false;
    }

    private static Boolean checkCommandPermissions(CommandEvent event, Command command) {
        Boolean hasAccess = CommandPermissions.hasAccess(command.getClass(), event.getMember(), event.getTextChannel(), false);
        if (hasAccess == null || Boolean.TRUE.equals(hasAccess)) {
            return hasAccess;
        }

        String desc = TextManager.getString(command.getLocale(), TextManager.GENERAL, "permission_block_description", command.getPrefix());
        if (BotPermissionUtil.canWriteEmbed(event.getTextChannel()) || event.isSlashCommandInteractionEvent()) {
            EmbedBuilder eb = EmbedFactory.getEmbedError()
                    .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "permission_block_title", command.getPrefix()))
                    .setDescription(desc);
            sendError(event, command.getLocale(), eb, true);
            return false;
        } else if (BotPermissionUtil.canWrite(event.getTextChannel())) {
            sendErrorNoEmbed(event, command.getLocale(), desc, true);
            return false;
        }
        return null;
    }

    private static boolean checkTurnedOn(CommandEvent event, Command command) {
        if (BotPermissionUtil.can(event.getMember(), Permission.ADMINISTRATOR)) {
            return true;
        }

        String desc = TextManager.getString(command.getLocale(), TextManager.GENERAL, "turnedoff_description");
        if (BotPermissionUtil.canWriteEmbed(event.getGuildMessageChannel()) || event.isSlashCommandInteractionEvent()) {
            EmbedBuilder eb = EmbedFactory.getEmbedError()
                    .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "turnedoff_title", command.getPrefix()))
                    .setDescription(desc);
            sendError(event, command.getLocale(), eb, true);
        } else if (BotPermissionUtil.canWrite(event.getGuildMessageChannel())) {
            sendErrorNoEmbed(event, command.getLocale(), desc, true);
        }
        return false;
    }

    private static boolean canRunOnGuild(CommandEvent event, Command command) {
        return command.canRunOnGuild(event.getGuild().getIdLong(), event.getMember().getIdLong());
    }

    private static boolean botCanUseEmbeds(CommandEvent event, Command command) {
        if (BotPermissionUtil.canWriteEmbed(event.getGuildMessageChannel()) || !command.getCommandProperties().requiresEmbeds() || event.isSlashCommandInteractionEvent()) {
            return true;
        }

        sendErrorNoEmbed(event, command.getLocale(), TextManager.getString(command.getLocale(), TextManager.GENERAL, "no_embed"), true);
        sendHelpDm(event.getMember(), command);
        return false;
    }

    private static void sendErrorNoEmbed(CommandEvent event, Locale locale, String text, boolean autoDelete, Button... buttons) {
        if (BotPermissionUtil.canWrite(event.getGuildMessageChannel()) || event.isSlashCommandInteractionEvent()) {
            RestAction<Message> messageAction = event.replyMessage(TextManager.getString(locale, TextManager.GENERAL, "command_block", text), ActionRows.of(buttons));
            if (autoDelete) {
                messageAction.queue(message -> autoRemoveMessageAfterCountdown(event, message));
            } else {
                messageAction.queue();
            }
        }
    }

    private static void sendError(CommandEvent event, Locale locale, EmbedBuilder eb, boolean autoDelete, Button... buttons) {
        if (BotPermissionUtil.canWriteEmbed(event.getGuildMessageChannel()) || event.isSlashCommandInteractionEvent()) {
            if (autoDelete) {
                eb.setFooter(TextManager.getString(locale, TextManager.GENERAL, "delete_time", String.valueOf(SEC_UNTIL_REMOVAL)));
            }

            RestAction<Message> messageAction = event.replyMessageEmbeds(List.of(eb.build()), ActionRows.of(buttons));
            if (autoDelete) {
                messageAction.queue(message -> autoRemoveMessageAfterCountdown(event, message));
            } else {
                messageAction.queue();
            }
        }
    }

    private static void autoRemoveMessageAfterCountdown(CommandEvent event, Message message) {
        MainScheduler.schedule(SEC_UNTIL_REMOVAL, ChronoUnit.SECONDS, "command_manager_error_countdown", () -> {
            if (BotPermissionUtil.can(event.getGuildMessageChannel())) {
                ArrayList<Message> messageList = new ArrayList<>();
                if (message != null) {
                    messageList.add(message);
                }
                if (event.isGuildMessageReceivedEvent() && BotPermissionUtil.can(event.getGuildMessageChannel(), Permission.MESSAGE_MANAGE)) {
                    messageList.add(event.getMessageReceivedEvent().getMessage());
                }
                if (messageList.size() >= 2) {
                    event.getGuildMessageChannel().deleteMessages(messageList).queue();
                } else if (messageList.size() >= 1) {
                    event.getGuildMessageChannel().deleteMessageById(messageList.get(0).getId()).queue();
                }
            }
        });
    }

    private static boolean botCanPost(CommandEvent event, Command command) {
        if (BotPermissionUtil.canWrite(event.getGuildMessageChannel()) || event.isSlashCommandInteractionEvent()) {
            return true;
        }

        if (command.getGuild().isPresent() &&
                event.isGuildMessageReceivedEvent() &&
                BotPermissionUtil.canReadHistory(event.getGuildMessageChannel(), Permission.MESSAGE_ADD_REACTION)
        ) {
            Message message = event.getMessageReceivedEvent().getMessage();
            RestActionQueue restActionQueue = new RestActionQueue();
            restActionQueue.attach(message.addReaction(Emoji.fromUnicode(Emojis.X)));
            restActionQueue.attach(message.addReaction(Emoji.fromUnicode("✍️")))
                    .getCurrentRestAction()
                    .queue();
        }

        return false;
    }

    private static boolean sendHelpDm(Member member, Command command) {
        return false;
    }

    private static void sendOverwrittenSignals(Command command, User member) {
        sendOverwrittenSignals(command, member, OnReactionListener.class);
        sendOverwrittenSignals(command, member, OnMessageInputListener.class);
        sendOverwrittenSignals(command, member, OnButtonListener.class);
        sendOverwrittenSignals(command, member, OnStringSelectMenuListener.class);
    }

    private static void sendOverwrittenSignals(Command command, User member, Class<?> clazz) {
        if (clazz.isInstance(command)) {
            CommandContainer.getListeners(clazz).stream()
                    .filter(meta -> meta.getAuthorId() == member.getIdLong())
                    .forEach(CommandListenerMeta::override);
        }
    }

    private static void cleanPreviousListeners(Command command, User member) {
        for (Class<?> clazz : CommandContainer.getListenerClasses()) {
            if (clazz.isInstance(command)) {
                ArrayList<CommandListenerMeta<?>> metaList = CommandContainer.getListeners(clazz).stream()
                        .filter(meta -> meta.getAuthorId() == member.getIdLong())
                        .sorted(Comparator.comparing(CommandListenerMeta::getCreationTime))
                        .collect(Collectors.toCollection(ArrayList::new));

                while (metaList.size() >= 2) {
                    CommandListenerMeta<?> meta = metaList.remove(0);
                    CommandContainer.deregisterListeners(meta.getCommand());
                    meta.timeOut();
                }
            }
        }
    }

    public static Optional<Command> createCommandByTrigger(String trigger, Locale locale, String prefix) {
        Class<? extends Command> clazz = CommandContainer.getCommandMap().get(trigger);
        if (clazz == null) return Optional.empty();
        return Optional.of(createCommandByClass(clazz, locale, prefix));
    }

    public static Command createCommandByClassName(String className, Locale locale, String prefix) {
        try {
            return createCommandByClass((Class<? extends Command>) Class.forName(className), locale, prefix);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Command createCommandByClass(Class<? extends Command> clazz, Locale locale, String prefix) {
        if (clazz.getSuperclass() == DMCommand.class) {
            for (Constructor<?> s : clazz.getConstructors()) {
                if (s.getParameterCount() == 0) {
                    try {
                        return (Command) s.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }


        for (Constructor<?> s : clazz.getConstructors()) {
            if (s.getParameterCount() == 2) {
                try {
                    return (Command) s.newInstance(locale, prefix);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("Invalid class");
    }

}
