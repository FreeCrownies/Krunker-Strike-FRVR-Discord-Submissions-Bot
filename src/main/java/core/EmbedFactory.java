package core;

import commands.Command;
import constants.ExternalLinks;
import core.utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

public abstract class EmbedFactory {

    public static final Color FAILED_EMBED_COLOR = Color.red;
    public static final Color SUCCESS_EMBED_COLOR = Color.green;
    public static final Color DEFAULT_EMBED_COLOR = new Color(254, 254, 254);
    public static final Color PREMIUM_COLOR = Color.yellow;
    public static final Color UFO_COLOR = new Color(159, 214, 253);
    public static final Color HALLOWEEN_COLOR = new Color(255, 117, 24);

    public static Color getDefaultEmbedColor() {
        return DEFAULT_EMBED_COLOR;
    }

    public static EmbedBuilder getEmbedDefault(Command command) {
        return getEmbedDefault(command, null);
    }

    public static EmbedBuilder getEmbedDefault(Command command, String description) {
        EmbedBuilder eb = getEmbedDefault()
                .setTitle(command.getCommandProperties().emoji() + " " + command.getCommandLanguage().getTitle());

        if (description != null && description.length() > 0) {
            eb.setDescription(description);
        }
        EmbedUtil.setFooter(eb, command);
        return eb;
    }

    public static EmbedBuilder getEmbedDefault(Command command, String description, String title) {
        return getEmbedDefault(command, description)
                .setTitle(command.getCommandProperties().emoji() + " " + title);
    }

    public static EmbedBuilder getEmbedError(Command command) {
        return getEmbedError(command, null);
    }


    public static EmbedBuilder getEmbedError(Command command, String description) {
        EmbedBuilder eb = getEmbedError()
                .setColor(FAILED_EMBED_COLOR)
                .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "error"));

        if (description != null && description.length() > 0) {
            eb.setDescription(description);
        }
        EmbedUtil.setFooter(eb, command);
        return eb;
    }

    public static EmbedBuilder getEmbedError(Locale locale, String description, User user) {
        EmbedBuilder eb = getEmbedError()
                .setColor(FAILED_EMBED_COLOR)
                .setTitle(TextManager.getString(locale, TextManager.GENERAL, "error"));

        if (description != null && description.length() > 0) {
            eb.setDescription(description);
        }
        EmbedUtil.setFooter(eb, user);
        return eb;
    }

    public static EmbedBuilder getEmbedError(Command command, String description, String title) {
        return getEmbedError(command, description)
                .setTitle(title);
    }

    public static EmbedBuilder getEmbedInvalidArgs(Command command, String description) {
        EmbedBuilder eb = getEmbedError()
                .setColor(FAILED_EMBED_COLOR)
                .setTitle(TextManager.getString(command.getLocale(), TextManager.GENERAL, "invalid_args"));

        if (description != null && description.length() > 0) {
            eb.setDescription(description);
        }
        EmbedUtil.setFooter(eb, command);
        return eb;
    }

    public static EmbedBuilder getApiDownEmbed(Locale locale, String service) {
        return EmbedFactory.getEmbedError()
                .setTitle(TextManager.getString(locale, TextManager.GENERAL, "quiz_down_title"))
                .setDescription(TextManager.getString(locale, TextManager.GENERAL, "api_down", service));
    }

    public static EmbedBuilder getCommandDMEmbed(ArrayList<ActionRow> actionRowList) {
        return EmbedFactory.getEmbedError()
                .setTitle("❌ Incorrect bot usage")
                .setDescription("Please use `/submit` to submit your content to the official Krunker Strike FRVR server!");
    }

    public static EmbedBuilder getEmbedDefault() {
        return new EmbedBuilder()
                .setColor(getDefaultEmbedColor());
    }

    public static EmbedBuilder getEmbedError() {
        return new EmbedBuilder()
                .setColor(FAILED_EMBED_COLOR);
    }

    public static Button getSupportServerInviteButton(Locale locale) {
        return Button.of(ButtonStyle.LINK, ExternalLinks.SUPPORT_SERVER_INVITE_URL, TextManager.getString(locale, TextManager.GENERAL, "button_support_server"));
    }

    public static EmbedBuilder getAbortEmbed(Command command) {
        return EmbedFactory.getEmbedDefault(
                command,
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "process_abort_description"),
                TextManager.getString(command.getLocale(), TextManager.GENERAL, "process_abort_title")
        );
    }

}
