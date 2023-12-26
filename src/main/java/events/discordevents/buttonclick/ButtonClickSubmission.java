package events.discordevents.buttonclick;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.*;
import constants.SubmissionType;
import core.*;
import core.utils.JDAUtil;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.ButtonClickAbstract;
import mysql.modules.bannedusers.DBBannedUsers;
import mysql.modules.submission.DBSubmission;
import mysql.modules.submission.SubmissionSlot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@DiscordEvent
public class ButtonClickSubmission extends ButtonClickAbstract {

    private static final WebhookClient skinWebhookClient = WebhookClient.withUrl(System.getenv("WEBHOOK_SKIN"));
    private static final WebhookClient gameplayWebhookClient = WebhookClient.withUrl(System.getenv("WEBHOOK_GAMEPLAY"));
    private static final WebhookClient fanartWebhookClient = WebhookClient.withUrl(System.getenv("WEBHOOK_FANART"));

    @Override
    public boolean onButtonClick(ButtonInteractionEvent event) throws Throwable {
        if (!event.getComponentId().startsWith("submission:")) {
            return true;
        }

        String submissionId = event.getComponentId().split(":")[1];
        SubmissionSlot submissionSlot = DBSubmission.getInstance().get().getSubmissions().get(event.getMessageIdLong());

        if (submissionSlot == null) {
            event.replyEmbeds(EmbedFactory.getEmbedError().setTitle("Submission not found").setDescription("The submission could not be found. It might have been approved or denied already.").build()).setEphemeral(true).queue();
            return false;
        }

        switch (submissionId) {
            case "approve":
                approve(event, submissionSlot);
                break;
            case "deny":
                deny(event, submissionSlot);
                break;
            case "block":
                block(event, submissionSlot);
                break;
        }

        return false;
    }

    private void approve(ButtonInteractionEvent event, SubmissionSlot submissionSlot) {
        DBSubmission.getInstance().get().getSubmissions().remove(event.getMessageIdLong());

        boolean sent = switch(submissionSlot.getSubmissionType()) {
            case GAMEPLAY -> sendGameplay(submissionSlot);
            case SKIN -> sendSkin(submissionSlot);
            case FAN_ART -> sendFanart(submissionSlot);
        };

        EmbedBuilder eb;

        if (sent) {
            eb = EmbedFactory.getEmbedDefault()
                    .setTitle("Submission Approved")
                    .setColor(Color.GREEN)
                    .setDescription("The submission has been approved and sent into the correct channel.");
        } else {
            eb = EmbedFactory.getEmbedError()
                    .setTitle("Submission Failed")
                    .setColor(Color.RED)
                    .setDescription("The submission has been approved, but could not be sent into the correct channel, since the user <@" + submissionSlot.getUserId() + "> does not seem to be a member on this server anymore.");
        }
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        event.getMessage().delete().queue();
        if (submissionSlot.getMessageIdVideo() != null) {
            event.getMessageChannel().retrieveMessageById(submissionSlot.getMessageIdVideo()).queue(msg -> msg.delete().queue());
        }
    }

    private void deny(ButtonInteractionEvent event, SubmissionSlot submissionSlot) {
        Modal modal = ModalMediator.createModal("Deny Submission", modalInteractionEvent -> {
            DBSubmission.getInstance().get().getSubmissions().remove(event.getMessageIdLong());

            String reason;
            ModalMapping modalMapping = modalInteractionEvent.getValue("reason");
            if (modalMapping == null) {
                reason = "No reason given.";
            } else {
                reason = modalMapping.getAsString();
            }

            EmbedBuilder eb = EmbedFactory.getEmbedDefault()
                    .setTitle("Submission Denied")
                    .setDescription("The submission of the user <@" + submissionSlot.getUserId() + "> has been denied.")
                    .addField("Reason", reason, false);
            modalInteractionEvent.replyEmbeds(eb.build()).setEphemeral(true).queue();

            sendDeny(submissionSlot, reason);

            event.getMessage().delete().queue();
            if (submissionSlot.getMessageIdVideo() != null) {
                event.getMessageChannel().retrieveMessageById(submissionSlot.getMessageIdVideo()).queue(msg -> msg.delete().queue());
            }
        }).addActionRow(TextInput.create("reason", "Reason", TextInputStyle.PARAGRAPH).setPlaceholder("Reason for denying the submission...").build()).build();

        event.replyModal(modal).queue();
    }

    private void block(ButtonInteractionEvent event, SubmissionSlot submissionSlot) {
        Modal modal = ModalMediator.createModal("Deny Submission", modalInteractionEvent -> {
            DBSubmission.getInstance().get().getSubmissions().remove(event.getMessageIdLong());

            String reason;
            ModalMapping modalMapping = modalInteractionEvent.getValue("reason");
            if (modalMapping == null) {
                reason = "No reason given.";
            } else {
                reason = modalMapping.getAsString();
            }

            EmbedBuilder eb = EmbedFactory.getEmbedDefault()
                    .setTitle("User Blocked")
                    .setDescription("The user <@" + submissionSlot.getUserId() + "> will no longer be able to make submissions.")
                    .addField("Reason", reason, false);
            modalInteractionEvent.replyEmbeds(eb.build()).setEphemeral(true).queue();

            sendBlock(submissionSlot, reason);

            DBBannedUsers.getInstance().retrieve().getUserIds().add(submissionSlot.getUserId());

            event.getMessage().delete().queue();
            if (submissionSlot.getMessageIdVideo() != null) {
                event.getMessageChannel().retrieveMessageById(submissionSlot.getMessageIdVideo()).queue(msg -> msg.delete().queue());
            }
        }).addActionRow(TextInput.create("reason", "Reason", TextInputStyle.PARAGRAPH).setPlaceholder("Reason for denying the submission...").build()).build();

        event.replyModal(modal).queue();
    }

    private boolean sendGameplay(SubmissionSlot submissionSlot) {
        Guild guild = ShardManager.getLocalGuildById(1160968659091599380L).get();
        Member member = MemberCacheController.getInstance().loadMember(guild, submissionSlot.getUserId()).join();
        if (member == null) {
            return false;
        }

        WebhookMessage webhookMessageFile = new WebhookMessageBuilder()
                .setAllowedMentions(AllowedMentions.none())
                .setContent((submissionSlot.getDescription()  != null ? submissionSlot.getDescription() : "") + "\n\n[Gameplay Video](" + submissionSlot.getMediaUrl() + ") by <@" + submissionSlot.getUserId() + ">")
                .setAvatarUrl(member.getEffectiveAvatarUrl())
                .setUsername(member.getEffectiveName())
                .build();

        gameplayWebhookClient.send(webhookMessageFile)
                .exceptionally(ExceptionLogger.get());

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Gameplay Video")
                .setDescription("Your gameplay video has been accepted and sent into <#1160977801575411825>.")
                .addField("Media", submissionSlot.getMediaUrl(), false);
        if (submissionSlot.getDescription() != null) {
            eb.addField("Description", submissionSlot.getDescription(), false);
        }
        JDAUtil.sendPrivateMessage(submissionSlot.getUserId(), eb.build()).queue(s -> {
        }, f -> {
        });
        return true;
    }

    private boolean sendSkin(SubmissionSlot submissionSlot) {
        Guild guild = ShardManager.getLocalGuildById(1160968659091599380L).get();
        Member member = MemberCacheController.getInstance().loadMember(guild, submissionSlot.getUserId()).join();
        if (member == null) {
            return false;
        }

        WebhookMessage webhookMessage = new WebhookMessageBuilder()
                .setAllowedMentions(AllowedMentions.none())
                .setContent((submissionSlot.getDescription()  != null ? submissionSlot.getDescription() : "") + "\n\n[Skin](" + submissionSlot.getMediaUrl() + ") by <@" + submissionSlot.getUserId() + ">")
                .setAvatarUrl(member.getEffectiveAvatarUrl())
                .setUsername(member.getEffectiveName())
                .build();

        skinWebhookClient.send(webhookMessage)
                .exceptionally(ExceptionLogger.get());

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Skin Show-Off")
                .setColor(Color.GREEN)
                .setDescription("Your skin has been accepted and sent into <#1160978252433727638>.")
                .addField("Skin", submissionSlot.getMediaUrl(), false);
        if (submissionSlot.getDescription() != null) {
            eb.addField("Description", submissionSlot.getDescription(), false);
        }
        JDAUtil.sendPrivateMessage(submissionSlot.getUserId(), eb.build()).queue(s -> {
        }, f -> {
        });
        return true;
    }

    private boolean sendFanart(SubmissionSlot submissionSlot) {
        Guild guild = ShardManager.getLocalGuildById(1160968659091599380L).get();
        Member member = MemberCacheController.getInstance().loadMember(guild, submissionSlot.getUserId()).join();
        if (member == null) {
            return false;
        }

        WebhookMessage webhookMessage = new WebhookMessageBuilder()
                .setAllowedMentions(AllowedMentions.none())
                .setContent((submissionSlot.getDescription()  != null ? submissionSlot.getDescription() : "") + "\n\n[Fan-Art](" + submissionSlot.getMediaUrl() + ") by <@" + submissionSlot.getUserId() + ">")
                .setAvatarUrl(member.getEffectiveAvatarUrl())
                .setUsername(member.getEffectiveName())
                .build();

        fanartWebhookClient.send(webhookMessage)
                .exceptionally(ExceptionLogger.get());

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Fan-Art")
                .setColor(Color.GREEN)
                .setDescription("Your fan-art has been accepted and sent into <#1160978252433727638>.")
                .addField("Fan-Art", submissionSlot.getMediaUrl(), false);
        if (submissionSlot.getDescription() != null) {
            eb.addField("Description", submissionSlot.getDescription(), false);
        }
        JDAUtil.sendPrivateMessage(submissionSlot.getUserId(), eb.build()).queue(s -> {
        }, f -> {
        });
        return true;
    }

    private void sendDeny(SubmissionSlot submissionSlot, String reason) {
        EmbedBuilder eb = EmbedFactory.getEmbedError()
                .setTitle("Submission Denied")
                .setDescription("Your submission has been denied.")
                .addField("Your Media", submissionSlot.getMediaUrl(), false)
                .addField("Reason", reason, false);
        JDAUtil.sendPrivateMessage(submissionSlot.getUserId(), eb.build()).queue(s -> {
        }, f -> {
        });
    }

    private void sendBlock(SubmissionSlot submissionSlot, String reason) {
        MainLogger.get().info("User " + submissionSlot.getUserId() + " has been blocked from the submissions system.");
        EmbedBuilder eb = EmbedFactory.getEmbedError()
                .setTitle("You have been blocked")
                .setDescription("You have been blocked from the Krunker Strike FRVR submissions system.\nYou cannot make any more submissions or interact with this bot.")
                .addField("Reason", reason, false);
        JDAUtil.sendPrivateMessage(submissionSlot.getUserId(), eb.build()).queue(s -> {
        }, f -> {
        });
    }

}
