package core.utils;

import constants.ExternalLinks;
import core.TextManager;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.Locale;

public class ButtonUtil {

    public static final String BUTTON_ID_CANCEL = "nav:cancel",
            BUTTON_ID_CONFIRM = "nav:confirm",
            BUTTON_ID_BACK = "nav:back",
            BUTTON_ID_PREV = "nav:prev",
            BUTTON_ID_NEXT = "nav:next",
            BUTTON_ID_RESET = "nav:reset";

    public static Button getCancelButton(Locale locale) {
        return Button.of(
                ButtonStyle.SECONDARY,
                BUTTON_ID_CANCEL,
                TextManager.getString(locale, TextManager.GENERAL, "button_label_cancel")
        );
    }

    public static Button getConfirmationButton(Locale locale) {
        return Button.of(
                ButtonStyle.DANGER,
                BUTTON_ID_CONFIRM,
                TextManager.getString(locale, TextManager.GENERAL, "button_label_confirm")
        );
    }

    public static Button getBackButton(Locale locale) {
        return Button.of(
                ButtonStyle.SECONDARY,
                BUTTON_ID_BACK,
                TextManager.getString(locale, TextManager.GENERAL, "button_label_back")
        );
    }

    public static Button getPreviousButton(Locale locale) {
        return Button.of(
                ButtonStyle.SECONDARY,
                BUTTON_ID_PREV,
                TextManager.getString(locale, TextManager.GENERAL, "button_label_previous")
        );
    }

    public static Button getPreviousButton(Locale locale, int id) {
        return Button.of(
                ButtonStyle.SECONDARY,
                String.valueOf(id),
                TextManager.getString(locale, TextManager.GENERAL, "button_label_previous")
        );
    }

    public static Button getNextButton(Locale locale) {
        return Button.of(
                ButtonStyle.SECONDARY,
                BUTTON_ID_NEXT,
                TextManager.getString(locale, TextManager.GENERAL, "button_label_next")
        );
    }

    public static Button getNextButton(Locale locale, int id) {
        return Button.of(
                ButtonStyle.SECONDARY,
                String.valueOf(id),
                TextManager.getString(locale, TextManager.GENERAL, "button_label_next")
        );
    }

    public static Button getResetButton(Locale locale) {
        return Button.of(
                ButtonStyle.SECONDARY,
                BUTTON_ID_RESET,
                TextManager.getString(locale, TextManager.GENERAL, "button_label_reset")
        );
    }

    public static Button getConfirmationButton(Locale locale, String id) {
        return Button.of(
                ButtonStyle.DANGER,
                id,
                TextManager.getString(locale, TextManager.GENERAL, "button_label_confirm")
        );
    }

    public static Button getResetButton(Locale locale, int id) {
        return Button.of(
                ButtonStyle.DANGER,
                String.valueOf(id),
                TextManager.getString(locale, TextManager.GENERAL, "button_label_reset")
        );
    }

    public static Button getResetButton(Locale locale, int id, boolean enabled) {
        Button button = Button.of(
                ButtonStyle.DANGER,
                String.valueOf(id),
                TextManager.getString(locale, TextManager.GENERAL, "button_label_reset")
        );
        return enabled ? button : button.asDisabled();
    }

    public static Button getDeleteButton(Locale locale, int id) {
        return Button.of(
                ButtonStyle.DANGER,
                String.valueOf(id),
                TextManager.getString(locale, TextManager.GENERAL, "button_label_delete")
        );
    }

    public static Button getContinueButton(Locale locale, int id) {
        return Button.of(
                ButtonStyle.PRIMARY,
                String.valueOf(id),
                TextManager.getString(locale, TextManager.GENERAL, "button_label_continue")
        );
    }

    public static Button getContinueButton(Locale locale, int id, boolean enabled) {
        Button button = Button.of(
                ButtonStyle.PRIMARY,
                String.valueOf(id),
                TextManager.getString(locale, TextManager.GENERAL, "button_label_continue")
        );
        return enabled ? button : button.asDisabled();
    }

    public static Button getSupportServerInviteButton(Locale locale) {
        return Button.link(
                ExternalLinks.SUPPORT_SERVER_INVITE_URL,
                TextManager.getString(locale, TextManager.GENERAL, "button_support_server")
        );
    }

    public static Button getBotInviteButton(Locale locale) {
        return Button.link(
                ExternalLinks.BOT_INVITE_URL,
                TextManager.getString(locale, TextManager.GENERAL, "button_invite")
        );
    }

    public static Button getPatreonButton(Locale locale) {
        return Button.link(
                ExternalLinks.PATREON_URL,
                TextManager.getString(locale, TextManager.GENERAL, "button_patreon")
        );
    }

    public static Button[] getPageButtons(Locale locale, String idPrev, String idNext) {
        return new Button[]{
                Button.of(
                        ButtonStyle.SECONDARY,
                        idPrev,
                        TextManager.getString(locale, TextManager.GENERAL, "button_label_previous")
                ),
                Button.of(
                        ButtonStyle.SECONDARY,
                        idNext,
                        TextManager.getString(locale, TextManager.GENERAL, "button_label_next")
                )
        };
    }

}
