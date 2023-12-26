package events.discordevents;

import core.DiscordConnector;
import core.GlobalThreadPool;
import core.MainLogger;
import core.ShardManager;
import events.discordevents.eventtypeabstracts.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateUserLimitEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostCountEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.ApplicationCommandUpdatePrivilegesEvent;
import net.dv8tion.jda.api.events.interaction.command.ApplicationUpdatePrivilegesEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class DiscordEventAdapter extends ListenerAdapter {

    private final HashMap<Class<?>, ArrayList<DiscordEventAbstract>> listenerMap;

    public DiscordEventAdapter() {
        listenerMap = new HashMap<>();

        Reflections reflections = new Reflections("events/discordevents");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(DiscordEvent.class);
        Set<Class<? extends DiscordEventAbstract>> listenerTypeAbstracts = reflections.getSubTypesOf(DiscordEventAbstract.class);

        annotated.stream()
                .map(clazz -> {
                    try {
                        return clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        MainLogger.get().error("Error when creating listener class", e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .filter(obj -> obj instanceof DiscordEventAbstract)
                .map(obj -> (DiscordEventAbstract) obj)
                .forEach(listener -> putListener(listener, listenerTypeAbstracts));
    }

    private void putListener(DiscordEventAbstract listener, Set<Class<? extends DiscordEventAbstract>> listenerTypeAbstracts) {
        for (Class<?> clazz : listenerTypeAbstracts) {
            if (clazz.isInstance(listener)) {
                ArrayList<DiscordEventAbstract> listenerList = listenerMap.computeIfAbsent(clazz, k -> new ArrayList<>());
                listenerList.add(listener);
            }
        }
    }

    private ArrayList<DiscordEventAbstract> getListenerList(Class<? extends DiscordEventAbstract> clazz) {
        return listenerMap.computeIfAbsent(clazz, k -> new ArrayList<>());
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        ShardManager.initAssetIds(event.getJDA());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> DiscordConnector.onJDAJoin(event.getJDA()));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel() instanceof GuildMessageChannel) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildMessageReceivedAbstract.onGuildMessageReceivedStatic(event, getListenerList(GuildMessageReceivedAbstract.class)));
            GlobalThreadPool.getExecutorService()
                    .submit(() -> WebhookReceivedAbstract.onWebhookReceivedStatic(event, getListenerList(WebhookReceivedAbstract.class)));
        } else if (event.getChannel() instanceof PrivateChannel) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> PrivateMessageReceivedAbstract.onPrivateMessageReceivedStatic(event, getListenerList(PrivateMessageReceivedAbstract.class)));
        }
    }

    @Override
    public void onUserTyping(@NotNull UserTypingEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> UserTypingAbstract.onUserTypingStatic(event, getListenerList(UserTypingAbstract.class)));
        }
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildMessageUpdateAbstract.onGuildMessageUpdateStatic(event, getListenerList(GuildMessageUpdateAbstract.class)));
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildMessageDeleteAbstract.onGuildMessageDeleteStatic(event, getListenerList(GuildMessageDeleteAbstract.class)));
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildMessageReactionAddAbstract.onGuildMessageReactionAddStatic(event, getListenerList(GuildMessageReactionAddAbstract.class)));
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildMessageReactionRemoveAbstract.onGuildMessageReactionRemoveStatic(event, getListenerList(GuildMessageReactionRemoveAbstract.class)));
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildJoinAbstract.onGuildJoinStatic(event, getListenerList(GuildJoinAbstract.class)));
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildLeaveAbstract.onGuildLeaveStatic(event, getListenerList(GuildLeaveAbstract.class)));
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildMemberJoinAbstract.onGuildMemberJoinStatic(event, getListenerList(GuildMemberJoinAbstract.class)));
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildMemberRemoveAbstract.onGuildMemberRemoveStatic(event, getListenerList(GuildMemberRemoveAbstract.class)));
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildUnbanAbstract.onGuildUnbanStatic(event, getListenerList(GuildUnbanAbstract.class)));
    }

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> TextChannelCreateAbstract.onTextChannelCreateStatic(event, getListenerList(TextChannelCreateAbstract.class)));
        } else if (event.getChannel() instanceof VoiceChannel) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> VoiceChannelCreateAbstract.onVoiceChannelCreateStatic(event, getListenerList(VoiceChannelCreateAbstract.class)));
        }
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> TextChannelDeleteAbstract.onTextChannelDeleteStatic(event, getListenerList(TextChannelDeleteAbstract.class)));
        } else if (event.getChannel() instanceof VoiceChannel) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> VoiceChannelDeleteAbstract.onVoiceChannelDeleteStatic(event, getListenerList(VoiceChannelDeleteAbstract.class)));
        }
    }

    @Override
    public void onGenericPermissionOverride(@NotNull GenericPermissionOverrideEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GenericPermissionOverrideAbstract.onGenericPermissionOverrideStatic(event, getListenerList(GenericPermissionOverrideAbstract.class)));
        }
    }

    @Override
    public void onChannelUpdateUserLimit(@NotNull ChannelUpdateUserLimitEvent event) {
        if (event.getChannel() instanceof VoiceChannel) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> VoiceChannelUpdateUserLimitAbstract.onVoiceChannelUpdateUserLimitStatic(event, getListenerList(VoiceChannelUpdateUserLimitAbstract.class)));
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        VoiceChannel voiceChannelJoined = event.getChannelJoined() instanceof VoiceChannel ? (VoiceChannel) event.getChannelJoined() : null;
        VoiceChannel voiceChannelLeft = event.getChannelLeft() instanceof VoiceChannel ? (VoiceChannel) event.getChannelLeft() : null;

        if (voiceChannelLeft != null && voiceChannelJoined != null) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildVoiceMoveAbstract.onGuildVoiceMoveStatic(event, getListenerList(GuildVoiceMoveAbstract.class)));
        } else if (voiceChannelJoined != null) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildVoiceJoinAbstract.onGuildVoiceJoinStatic(event, getListenerList(GuildVoiceJoinAbstract.class)));
        } else if (voiceChannelLeft != null) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> GuildVoiceLeaveAbstract.onGuildVoiceLeaveStatic(event, getListenerList(GuildVoiceLeaveAbstract.class)));
        }
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildMemberRoleAddAbstract.onGuildMemberRoleAddStatic(event, getListenerList(GuildMemberRoleAddAbstract.class)));
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildMemberRoleRemoveAbstract.onGuildMemberRoleRemoveStatic(event, getListenerList(GuildMemberRoleRemoveAbstract.class)));
    }

    @Override
    public void onGuildUpdateBoostCount(@NotNull GuildUpdateBoostCountEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildUpdateBoostCountAbstract.onGuildUpdateBoostCountStatic(event, getListenerList(GuildUpdateBoostCountAbstract.class)));
    }

    @Override
    public void onUserActivityStart(@NotNull UserActivityStartEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> UserActivityStartAbstract.onUserActivityStartStatic(event, getListenerList(UserActivityStartAbstract.class)));
    }

    @Override
    public void onGuildMemberUpdatePending(@NotNull GuildMemberUpdatePendingEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> GuildMemberUpdatePendingAbstract.onGuildMemberUpdatePendingStatic(event, getListenerList(GuildMemberUpdatePendingAbstract.class)));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> SlashCommandAbstract.onSlashCommandStatic(event, getListenerList(SlashCommandAbstract.class)));
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> ButtonClickAbstract.onButtonClickStatic(event, getListenerList(ButtonClickAbstract.class)));
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (event.getChannel() instanceof GuildMessageChannelUnion) {
            GlobalThreadPool.getExecutorService()
                    .submit(() -> SelectMenuInteraction.onSelectMenuStatic(event, getListenerList(SelectMenuInteraction.class)));
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> ModalInteractionAbstract.onModalInteractionStatic(event, getListenerList(ModalInteractionAbstract.class)));
    }

    @Override
    public void onApplicationCommandUpdatePrivileges(@NotNull ApplicationCommandUpdatePrivilegesEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> ApplicationCommandUpdatePrivilegesAbstract.onApplicationCommandUpdatePrivilegesStatic(event, getListenerList(ApplicationCommandUpdatePrivilegesAbstract.class)));
    }

    @Override
    public void onApplicationUpdatePrivileges(@NotNull ApplicationUpdatePrivilegesEvent event) {
        GlobalThreadPool.getExecutorService()
                .submit(() -> ApplicationUpdatePrivilegesAbstract.onApplicationUpdatePrivilegesStatic(event, getListenerList(ApplicationUpdatePrivilegesAbstract.class)));
    }

}
