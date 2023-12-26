package commands.runnables.dm;

import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.listeners.MessageInputResponse;
import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import constants.RegexPatterns;
import constants.SubmissionType;
import core.*;
import core.utils.ExceptionUtil;
import mysql.modules.submission.DBSubmission;
import mysql.modules.submission.SubmissionSlot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandProperties(
        trigger = "submit",
        emoji = "\uD83D\uDCDD",
        aliases = {"submission", "submissions"}
)
public class SubmitCommand extends NavigationAbstract {

    private static final int
            HOME = 0,
            SUBMIT_GAMEPLAY = 1,
            SUBMIT_SKIN = 2,
            SUBMIT_ART = 4,
            SUBMISSION_COMPLETED = 3;

    private String mediaUrl, description;

    public SubmitCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(CommandEvent event, String args) throws Throwable {
        registerNavigationListener(event.getUser());
        return true;
    }


    @ControllerMessage(state = SUBMIT_GAMEPLAY)
    public MessageInputResponse onMessageSubmitGameplay(MessageReceivedEvent event, String input) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();

        if (!attachments.isEmpty()) {
            Message.Attachment attachment = attachments.get(0);
            mediaUrl = attachment.getUrl();
        } else {
            Pattern p = RegexPatterns.VIDEO_URL;
            Matcher m = p.matcher(input);
            if (m.find()) {
                mediaUrl = m.group();
            } else {
                setLog(LogStatus.FAILURE, TextManager.getNoResultsString(getLocale(), event.getMessage().getContentRaw()));
                return MessageInputResponse.FAILED;
            }
        }
        setLog(LogStatus.SUCCESS, getString("set_media"));
        return MessageInputResponse.SUCCESS;
    }

    @ControllerMessage(state = SUBMIT_SKIN)
    public MessageInputResponse onMessageSubmitSkin(MessageReceivedEvent event, String input) {
        return onMessageImage(event, input);
    }

    @ControllerMessage(state = SUBMIT_ART)
    public MessageInputResponse onMessageSubmitArt(MessageReceivedEvent event, String input) {
        return onMessageImage(event, input);
    }


    @ControllerButton(state = HOME)
    public boolean onButtonHome(ButtonInteractionEvent event, int i) {
        switch (i) {
            case 0 -> {
                setState(SUBMIT_GAMEPLAY);
                return true;
            }
            case 1 -> {
                setState(SUBMIT_SKIN);
                return true;
            }
            case 2 -> {
                setState(SUBMIT_ART);
                return true;
            }
        }
        return false;
    }

    @ControllerButton(state = SUBMIT_GAMEPLAY)
    public boolean onButtonSubmitGameplay(ButtonInteractionEvent event, int i) {
        switch (i) {
            case -1 -> {
                setState(HOME);
                return true;
            }
            case 0 -> {
                event.replyModal(runDescriptionModal()).queue();
                return false;
            }
            case 1 -> {
                description = null;
                return true;
            }
            case 2 -> {
                if (DBSubmission.getInstance().get().getSubmission(event.getUser().getIdLong(), SubmissionType.GAMEPLAY).isPresent()) {
                    setLog(LogStatus.FAILURE, getString("already_submitted"));
                    return true;
                }

                sendSubmissionRequest(event.getUser());
                setState(SUBMISSION_COMPLETED);
                deregisterListenersWithComponents();
                return true;
            }
        }
        return false;
    }

    @ControllerButton(state = SUBMIT_SKIN)
    public boolean onButtonSubmitSkin(ButtonInteractionEvent event, int i) {
        switch (i) {
            case -1 -> {
                setState(HOME);
                return true;
            }
            case 0 -> {
                event.replyModal(runDescriptionModal()).queue();
                return false;
            }
            case 1 -> {
                description = null;
                return true;
            }
            case 2 -> {
                if (DBSubmission.getInstance().get().getSubmission(event.getUser().getIdLong(), SubmissionType.SKIN).isPresent()) {
                    setLog(LogStatus.FAILURE, getString("already_submitted"));
                    return true;
                }

                sendSubmissionRequest(event.getUser());
                setState(SUBMISSION_COMPLETED);
                deregisterListenersWithComponents();
                return true;
            }
        }
        return false;
    }

    @ControllerButton(state = SUBMIT_ART)
    public boolean onButtonSubmitArt(ButtonInteractionEvent event, int i) {
        switch (i) {
            case -1 -> {
                setState(HOME);
                return true;
            }
            case 0 -> {
                event.replyModal(runDescriptionModal()).queue();
                return false;
            }
            case 1 -> {
                description = null;
                return true;
            }
            case 2 -> {
                if (DBSubmission.getInstance().get().getSubmission(event.getUser().getIdLong(), SubmissionType.FAN_ART).isPresent()) {
                    setLog(LogStatus.FAILURE, getString("already_submitted"));
                    return true;
                }

                sendSubmissionRequest(event.getUser());
                setState(SUBMISSION_COMPLETED);
                deregisterListenersWithComponents();
                return true;
            }
        }
        return false;
    }


    @Draw(state = HOME)
    public EmbedBuilder onDrawHome(User user) throws Throwable {
        setComponents(optionsToButtons(getString("state0_options").split("\n")));
        return EmbedFactory.getEmbedDefault(this, getString("state0_description"), getString("state0_title"));
    }

    @Draw(state = SUBMIT_GAMEPLAY)
    public EmbedBuilder onDrawSubmitGameplay(User user) throws Throwable {
        String notset = TextManager.getString(getLocale(), TextManager.GENERAL, "not_set");
        setComponents(optionsToButtons(getString("state1_options").split("\n"), mediaUrl != null));
        return EmbedFactory
                .getEmbedDefault(this, getString("state1_description"), getString("state1_title"))
                .addField(getString("state1_field_media_title"), mediaUrl == null ? notset : mediaUrl, false)
                .addField(getString("state1_field_description_title"), description == null ? notset : description, false);
    }

    @Draw(state = SUBMIT_SKIN)
    public EmbedBuilder onDrawSubmitSkin(User user) throws Throwable {
        String notset = TextManager.getString(getLocale(), TextManager.GENERAL, "not_set");
        setComponents(optionsToButtons(getString("state1_options").split("\n"), mediaUrl != null));
        return EmbedFactory
                .getEmbedDefault(this, getString("state2_description"), getString("state2_title"))
                .addField(getString("state1_field_media_title"), mediaUrl == null ? notset : mediaUrl, false)
                .addField(getString("state1_field_description_title"), description == null ? notset : description, false);
    }

    @Draw(state = SUBMIT_ART)
    public EmbedBuilder onDrawSubmitArt(User user) throws Throwable {
        String notset = TextManager.getString(getLocale(), TextManager.GENERAL, "not_set");
        setComponents(optionsToButtons(getString("state1_options").split("\n"), mediaUrl != null));
        return EmbedFactory
                .getEmbedDefault(this, getString("state2_description"), getString("state2_title"))
                .addField(getString("state1_field_media_title"), mediaUrl == null ? notset : mediaUrl, false)
                .addField(getString("state1_field_description_title"), description == null ? notset : description, false);
    }

    @Draw(state = SUBMISSION_COMPLETED)
    public EmbedBuilder onDrawSubmissionCompleted(User user) throws Throwable {
        return EmbedFactory.getEmbedDefault(this, getString("state3_description"), getString("state3_title"));
    }


    private MessageInputResponse onMessageImage(MessageReceivedEvent event, String input) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();

        if (!attachments.isEmpty()) {
            Message.Attachment attachment = attachments.get(0);
            mediaUrl = attachment.getUrl();
        } else {
            Pattern p = RegexPatterns.IMAGE_URL;
            Matcher m = p.matcher(input);
            if (m.find()) {
                mediaUrl = m.group();
            } else {
                setLog(LogStatus.FAILURE, TextManager.getNoResultsString(getLocale(), event.getMessage().getContentRaw()));
                return MessageInputResponse.FAILED;
            }
        }
        setLog(LogStatus.SUCCESS, getString("set_media"));
        return MessageInputResponse.SUCCESS;
    }

    private Modal runDescriptionModal() {
        TextInput textInput = TextInput.create("0", getString("state1_modal_input_label"), TextInputStyle.PARAGRAPH)
                .setMinLength(10)
                .setMaxLength(500)
                .build();
        return ModalMediator.createModal(getString("state1_modal_title"), modalInteractionEvent -> {
                    modalInteractionEvent.deferEdit().queue();
                    description = modalInteractionEvent.getValue("0").getAsString();
                    setLog(LogStatus.SUCCESS, getString("set_description"));
                    try {
                        processDraw(modalInteractionEvent.getUser(), true)
                                .exceptionally(ExceptionLogger.get());
                    } catch (Throwable e) {
                        ExceptionUtil.handleCommandException(e, this);
                    }
                }).addActionRows(ActionRow.of(textInput))
                .build();
    }

    private void sendSubmissionRequest(User user) {
        SubmissionType submissionType = switch (getState()) {
            case SUBMIT_GAMEPLAY -> SubmissionType.GAMEPLAY;
            case SUBMIT_SKIN -> SubmissionType.SKIN;
            case SUBMIT_ART -> SubmissionType.FAN_ART;
            default -> throw new IllegalStateException("Unexpected value: " + getState());
        };
        String notset = TextManager.getString(getLocale(), TextManager.GENERAL, "not_set");
        TextChannel channel = ShardManager.getLocalGuildById(1160968659091599380L).get().getTextChannelById(1187396670456082453L);
        Long messageIdVideo;
        if (submissionType == SubmissionType.GAMEPLAY) {
            messageIdVideo = channel.sendMessage(mediaUrl).complete().getIdLong();
        } else {
            messageIdVideo = null;
        }
        EmbedBuilder eb = EmbedFactory.getEmbedDefault()
                .setTitle(getString("submission_title_" + submissionType.ordinal()) + " submission")
                .addField("User", user.getAsTag() + " (" + user.getAsMention() + ")", false)
                .addField("Description", description == null ? notset : description, false)
                .addField("Media", mediaUrl, false);
        if (submissionType != SubmissionType.GAMEPLAY) {
            eb.setImage(mediaUrl);
        }
        channel.sendMessageEmbeds(eb.build()).addActionRow(
                Button.success(
                        "submission:approve",
                        "Approve"
                ), Button.danger(
                        "submission:deny",
                        "Deny"
                ), Button.danger(
                        "submission:block",
                        "Block User"
                )
        ).queue(msg -> DBSubmission.getInstance().get().getSubmissions().put(
                        msg.getIdLong(),
                        new SubmissionSlot(
                                user.getIdLong(),
                                msg.getIdLong(),
                                messageIdVideo,
                                submissionType,
                                mediaUrl,
                                description
                        )
                )
        );
    }

}
