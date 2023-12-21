package commands.listeners;

import commands.CommandListenerMeta;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface OnStringSelectMenuListener extends OnInteractionListener {

    boolean onSelectMenu(StringSelectInteractionEvent event) throws Throwable;

    default CompletableFuture<Long> registerSelectMenuListener(Member member) {
        return registerSelectMenuListener(member, true);
    }

    default CompletableFuture<Long> registerSelectMenuListener(Member member, boolean draw) {
        return registerInteractionListener(member, this::onSelectMenuOverridden, OnStringSelectMenuListener.class, draw);
    }

    default CompletableFuture<Long> registerSelectMenuListener(Member member, Function<StringSelectInteractionEvent, CommandListenerMeta.CheckResponse> validityChecker, boolean draw) {
        return registerInteractionListener(member, validityChecker, this::onSelectMenuOverridden, OnStringSelectMenuListener.class, draw);
    }

    default void processSelectMenu(StringSelectInteractionEvent event) {
        processInteraction(event, this::onSelectMenu);
    }

    default void onSelectMenuOverridden() throws Throwable {
    }

}
