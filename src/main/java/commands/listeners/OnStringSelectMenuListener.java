package commands.listeners;

import commands.CommandListenerMeta;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface OnStringSelectMenuListener extends OnInteractionListener {

    boolean onSelectMenu(StringSelectInteractionEvent event) throws Throwable;

    default CompletableFuture<Long> registerSelectMenuListener(User member) {
        return registerSelectMenuListener(member, true);
    }

    default CompletableFuture<Long> registerSelectMenuListener(User member, boolean draw) {
        return registerInteractionListener(member, this::onSelectMenuOverridden, OnStringSelectMenuListener.class, draw);
    }

    default CompletableFuture<Long> registerSelectMenuListener(User member, Function<StringSelectInteractionEvent, CommandListenerMeta.CheckResponse> validityChecker, boolean draw) {
        return registerInteractionListener(member, validityChecker, this::onSelectMenuOverridden, OnStringSelectMenuListener.class, draw);
    }

    default void processSelectMenu(StringSelectInteractionEvent event) {
        processInteraction(event, this::onSelectMenu);
    }

    default void onSelectMenuOverridden() throws Throwable {
    }

}
