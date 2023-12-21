package commands.listeners;

import commands.Command;
import commands.CommandContainer;
import commands.CommandEvent;
import commands.DMCommand;
import core.MemberCacheController;
import core.interactionresponse.InteractionResponse;
import core.interactionresponse.SlashCommandResponse;
import core.schedule.MainScheduler;
import core.utils.ExceptionUtil;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public interface OnTriggerListener {

    boolean onTrigger(CommandEvent event, String args) throws Throwable;

    default boolean processTrigger(CommandEvent event, String args, boolean freshCommand) throws Throwable {
        Command command = (Command) this;
        if (freshCommand && event.isSlashCommandInteractionEvent()) {
            InteractionResponse interactionResponse = new SlashCommandResponse(event.getSlashCommandInteractionEvent().getHook());
            command.setInteractionResponse(interactionResponse);
        }

        AtomicBoolean isProcessing = new AtomicBoolean(true);
        if (!(command instanceof DMCommand)) {
            command.setAtomicAssets(event.getGuildMessageChannel(), event.getMember());
        }
        command.setCommandEvent(event);

        if (event.isGuildMessageReceivedEvent()) {
            command.addLoadingReaction(event.getMessageReceivedEvent().getMessage(), isProcessing);
        }
        addKillTimer(isProcessing);
        try {
            if (command.getCommandProperties().requiresFullMemberCache()) {
                MemberCacheController.getInstance().loadMembersFull(event.getGuild()).get();
            }
            return onTrigger(event, args);
        } catch (Throwable e) {
            ExceptionUtil.handleCommandException(e, command);
            return false;
        } finally {
            isProcessing.set(false);
        }
    }

    private void addKillTimer(AtomicBoolean isProcessing) {
        Command command = (Command) this;
        Thread commandThread = Thread.currentThread();
        MainScheduler.schedule(command.getCommandProperties().maxCalculationTimeSec(), ChronoUnit.SECONDS, "command_timeout", () -> {
            if (!command.getCommandProperties().turnOffTimeout()) {
                CommandContainer.addCommandTerminationStatus(command, commandThread, isProcessing.get());
            }
        });
    }

}