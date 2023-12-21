package commands.listeners;

import commands.CommandListenerMeta;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface OnButtonListener extends OnInteractionListener {

    boolean onButton(ButtonInteractionEvent event) throws Throwable;

    default CompletableFuture<Long> registerButtonListener(Member member) {
        return registerButtonListener(member, true);
    }

    default CompletableFuture<Long> registerButtonListener(Member member, boolean draw) {
        return registerInteractionListener(member, this::onButtonOverridden, OnButtonListener.class, draw);
    }

    default CompletableFuture<Long> registerButtonListener(Member member, Function<ButtonInteractionEvent, CommandListenerMeta.CheckResponse> validityChecker, boolean draw) {
        return registerInteractionListener(member, validityChecker, this::onButtonOverridden, OnButtonListener.class, draw);
    }

    default void processButton(ButtonInteractionEvent event) {
        processInteraction(event, this::onButton);
    }

    default void onButtonOverridden() throws Throwable {
    }

}