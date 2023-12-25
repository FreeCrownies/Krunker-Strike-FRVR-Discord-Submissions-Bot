package commands;

import commands.listeners.*;
import constants.LogStatus;
import core.MainLogger;
import core.Pair;
import core.Program;
import core.TextManager;
import core.atomicassets.AtomicGuild;
import core.atomicassets.AtomicGuildMessageChannel;
import core.atomicassets.AtomicMember;
import core.components.ActionRows;
import core.interactionresponse.InteractionResponse;
import core.schedule.MainScheduler;
import core.utils.*;
import mysql.modules.staticreactionmessages.DBStaticReactionMessages;
import mysql.modules.staticreactionmessages.StaticReactionMessageData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.json.JSONObject;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public abstract class Command implements OnTriggerListener {

    private final long id = ProgramUtil.generateId();
    private final Category category;
    private final String prefix;
    private final CommandProperties commandProperties;
    private final JSONObject attachments = new JSONObject();
    private final ArrayList<Runnable> completedListeners = new ArrayList<>();
    private final Map<String, InputStream> fileAttachmentMap = new HashMap<>();
    private Locale locale;
    private boolean loadingReactionSet = false;
    private AtomicBoolean isProcessing;
    private AtomicGuild atomicGuild;
    private AtomicGuildMessageChannel atomicGuildMessageChannel;
    private AtomicMember atomicMember;
    private Message drawMessage = null;
    private LogStatus logStatus = null;
    private String log = "";
    private CommandEvent commandEvent = null;
    private InteractionResponse interactionResponse;
    private boolean canHaveTimeOut = true;
    private List<ActionRow> actionRows = Collections.emptyList();
    private List<MessageEmbed> additionalEmbeds = Collections.emptyList();
    private Collection<Message.MentionType> allowedMentions = MessageRequest.getDefaultMentions();
    private String memberEffectiveName;
    private String memberMention;
    private String memberEffectiveAvatarUrl;
    private String memberTag;
    private boolean newMessageTest = false;

    public Command(Locale locale, String prefix) {
        this.locale = locale;
        this.prefix = prefix;
        commandProperties = this.getClass().getAnnotation(CommandProperties.class);
        category = CategoryCalculator.getCategoryByCommand(this.getClass());
    }

    public static Category getCategory(Class<? extends Command> clazz) {
        return CategoryCalculator.getCategoryByCommand(clazz);
    }

    public static CommandProperties getCommandProperties(Class<? extends Command> clazz) {
        return clazz.getAnnotation(CommandProperties.class);
    }

    public static CommandLanguage getCommandLanguage(Class<? extends Command> clazz, Locale locale) {
        String trigger = getCommandProperties(clazz).trigger();
        Category category = getCategory(clazz);

        String title = TextManager.getString(locale, category, trigger + "_title");
        String descShort = TextManager.getString(locale, category, trigger + "_description");
        String descLong = TextManager.getString(locale, category, trigger + "_helptext");
        String usage = TextManager.getString(locale, category, trigger + "_usage");
        String examples = TextManager.getString(locale, category, trigger + "_examples");
        return new CommandLanguage(title, descShort, descLong, usage, examples);
    }

    public Category getCategory() {
        return category;
    }

    public CommandProperties getCommandProperties() {
        return commandProperties;
    }

    public CommandLanguage getCommandLanguage() {
        String title = getString("title");
        String descShort = getString("description");
        String descLong = getString("helptext");
        String usage = getString("usage");
        String examples = getString("examples");
        return new CommandLanguage(title, descShort, descLong, usage, examples);
    }

    public void addLoadingReaction(Message message, AtomicBoolean isProcessing) {
        this.isProcessing = isProcessing;
        if (this instanceof DMCommand) return;
        MainScheduler.schedule(
                2500, ChronoUnit.MILLIS,
                getTrigger() + "_idle",
                () -> addLoadingReactionInstantly(message, isProcessing)
        );
    }

    public void addLoadingReactionInstantly() {
        if (isProcessing != null && commandEvent.isGuildMessageReceivedEvent()) {
            addLoadingReactionInstantly(commandEvent.getMessageReceivedEvent().getMessage(), isProcessing);
        }
    }

    public void addLoadingReactionInstantly(Message message, AtomicBoolean isProcessing) {
        if (message.getChannelType() == ChannelType.PRIVATE) return;
        GuildMessageChannelUnion channel = message.getGuildChannel();
        if (isProcessing.get() &&
                !loadingReactionSet && BotPermissionUtil.canReadHistory(channel, Permission.MESSAGE_ADD_REACTION) &&
                !getCommandProperties().turnOffLoadingReaction()
        ) {
            loadingReactionSet = true;

            String reaction = EmojiUtil.getLoadingEmojiTag(message.getGuildChannel());

            /*
            if (!DBGuild.getInstance().retrieve(message.getGuild().getIdLong()).isCommandAuthorMessageRemoveEffectively()) {
                message.addReaction(reaction).queue();
            }
             */
            MainScheduler.poll(100, getTrigger() + "_loading", () -> {
                if (isProcessing.get()) {
                    return true;
                } else {
                    /*
                    if (!DBGuild.getInstance().retrieve(message.getGuild().getIdLong()).isCommandAuthorMessageRemoveEffectively()) {
                        message.removeReaction(reaction).queue();
                    }
                     */
                    loadingReactionSet = false;
                    return false;
                }
            });
        }
    }

    public void setAdditionalEmbeds(MessageEmbed... additionalEmbeds) {
        this.additionalEmbeds = List.of(additionalEmbeds);
    }

    public void setAdditionalEmbeds(List<MessageEmbed> additionalEmbeds) {
        this.additionalEmbeds = additionalEmbeds;
    }

    public void addFileAttachment(InputStream data, String name) {
        this.fileAttachmentMap.put(name, data);
    }

    public void setAllowedMentions(Collection<Message.MentionType> allowedMentions) {
        this.allowedMentions = allowedMentions;
    }

    public List<ActionRow> getActionRows() {
        return actionRows;
    }

    public void setActionRows(ActionRow... actionRows) {
        this.actionRows = List.of(actionRows);
    }

    public void setActionRows(List<ActionRow> actionRows) {
        this.actionRows = actionRows;
    }

    public void setComponents(Button[] buttons, Pair<Integer, Boolean>... validations) {
        for (Pair<Integer, Boolean> validation : validations) {
            if (!validation.getRight()) {
                buttons[validation.getLeft()] = buttons[validation.getLeft()].asDisabled();
            }
        }
        setComponents(buttons);
    }

    public void setComponents(Button... buttons) {
        setComponents(List.of(buttons));
    }

    public void setComponents(SelectMenu... menus) {
        setComponents(List.of(menus));
    }

    public void setComponents(List<? extends ItemComponent> components) {
        this.actionRows = ActionRows.of(components);
    }

    public void setComponents(String... options) {
        if (options != null) {
            setComponents(optionsToButtons(options));
        } else {
            setActionRows();
        }
    }

    public void setComponents(String[] options, boolean... validations) {
        if (options != null) {
            setComponents(optionsToButtons(options, validations));
        } else {
            setActionRows();
        }
    }

//    public List<Button> optionsToButtons(String... options) {
//        ArrayList<Button> buttonList = new ArrayList<>();
//        if (options != null) {
//            for (int i = 0; i < options.length; i++) {
//                buttonList.add(
//                        Button.of(ButtonStyle.PRIMARY, String.valueOf(i), StringUtil.shortenString(options[i], 80))
//                );
//            }
//        }
//        return buttonList;
//    }

    public List<Button> optionsToButtons(String[] options, boolean... validations) {
        ArrayList<Button> buttonList = new ArrayList<>();
        if (options != null) {
            int i_disable = 0;
            for (int i = 0; i < options.length; i++) {
                try {
                    Button button;
                    String label = options[i];
                    boolean disabled = false;
                    ButtonStyle buttonStyle = ButtonStyle.PRIMARY;

                    if (label.contains("{DISABLE}")) {
                        label = label.replace("{DISABLE}", "");
                        if (!validations[i_disable]) {
                            disabled = true;
                        }
                        i_disable++;
                    }

                    if (label.contains("{RESET}")) {
                        button = ButtonUtil.getResetButton(locale, i);
                    } else {
                        if (label.contains("{SUCCESS}")) {
                            label = label.replace("{SUCCESS}", "");
                            buttonStyle = ButtonStyle.SUCCESS;
                        } else if (label.contains("{DANGER}")) {
                            label = label.replace("{DANGER}", "");
                            buttonStyle = ButtonStyle.DANGER;
                        }
                        button = Button.of(
                                buttonStyle,
                                String.valueOf(i),
                                StringUtil.shortenString(label, 80)
                        );
                    }
                    if (disabled) {
                        button = button.asDisabled();
                    }
                    buttonList.add(button);
                } catch (Exception e) {
                    MainLogger.get().error("Exception while parsing buttons", e);
                }
            }
        }
        return buttonList;
    }

    public CompletableFuture<Message> drawMessageNew(EmbedBuilder eb) {
        return drawMessage(eb, true);
    }

    public CompletableFuture<Message> drawMessageNew(String content) {
        return drawMessage(content, true);
    }

    public CompletableFuture<Message> drawMessage(EmbedBuilder eb) {
        return drawMessage(eb, false);
    }

    public CompletableFuture<Message> drawMessage(String content) {
        return drawMessage(content, false);
    }

    private CompletableFuture<Message> drawMessage(EmbedBuilder eb, boolean newMessage) {
        GuildMessageChannel channel = getGuildMessageChannel().orElse(null);
        if (channel != null) {
            if (BotPermissionUtil.canWriteEmbed(channel) || interactionResponse != null || commandEvent.isSlashCommandInteractionEvent()) {
                EmbedUtil.addLog(eb, logStatus, log);
                return drawMessage(channel, null, eb, newMessage);
            } else {
                return CompletableFuture.failedFuture(new PermissionException("Missing permissions"));
            }
        } else if (getGuild().isEmpty()) {
            EmbedUtil.addLog(eb, logStatus, log);
            commandEvent.getChannel();
            return drawMessage(commandEvent.getChannel().asPrivateChannel(), null, eb, newMessage);
        } else {
            return CompletableFuture.failedFuture(new NoSuchElementException("Missing text channel"));
        }
    }

    private CompletableFuture<Message> drawMessage(String content, boolean newMessage) {
        return getGuildMessageChannel()
                .map(channel -> drawMessage(channel, content, null, newMessage))
                .orElse(CompletableFuture.failedFuture(new NoSuchElementException("No such channel")));
    }

    private synchronized CompletableFuture<Message> drawMessage(GuildMessageChannel channel, String content, EmbedBuilder eb, boolean newMessage) {
        try {
            CompletableFuture<Message> future = new CompletableFuture<>();
            ArrayList<MessageEmbed> embeds = new ArrayList<>();
            try {
                if (eb != null) {
                    embeds.add(eb.build());
                }
                if (BotPermissionUtil.canWriteEmbed(channel)) {
                    embeds.addAll(additionalEmbeds);
                }
            } catch (Throwable e) {
                StringBuilder sb = new StringBuilder("Embed exception with fields:");
                if (eb != null) {
                    eb.getFields().forEach(field -> sb
                            .append("\nKey: \"")
                            .append(field.getName())
                            .append("\"; Value: \"")
                            .append(field.getValue())
                            .append("\"")
                    );
                }
                MainLogger.get().error(sb.toString(), e);
                throw e;
            }

            if (actionRows == null) {
                actionRows = Collections.emptyList();
            }

            HashSet<String> usedIds = new HashSet<>();
            for (ActionRow actionRow : actionRows) {
                for (ActionComponent component : actionRow.getActionComponents()) {
                    if (component.getId() != null) {
                        if (usedIds.contains(component.getId())) {
                            future.completeExceptionally(new Exception("Duplicate custom id \"" + component.getId() + "\""));
                            return future;
                        }
                        usedIds.add(component.getId());
                    }
                }
            }

            RestAction<Message> action;
            if (drawMessage == null || newMessage) {
                if (commandEvent.isGuildMessageReceivedEvent()) {
                    MessageCreateAction messageAction;
                    Message message = commandEvent.getMessageReceivedEvent().getMessage();
                    if (content != null) {
                        messageAction = JDAUtil.replyMessage(message, content)
                                .setEmbeds(embeds);
                    } else {
                        messageAction = JDAUtil.replyMessageEmbeds(false, message, embeds);
                    }
                    if (BotPermissionUtil.canWrite(channel, Permission.MESSAGE_ATTACH_FILES)) {
                        if (fileAttachmentMap.size() > 0) {
                            for (String fileName : fileAttachmentMap.keySet()) {
                                messageAction = messageAction.addFiles(FileUpload.fromData(fileAttachmentMap.get(fileName), fileName));
                            }
                        }
                    }
                    messageAction = messageAction.setAllowedMentions(allowedMentions);
                    action = messageAction.setComponents(actionRows);
                } else if (commandEvent.isSlashCommandInteractionEvent()) {
                    WebhookMessageCreateAction<Message> messageAction;
                    InteractionHook interactionHook = commandEvent.getSlashCommandInteractionEvent().getHook();
                    if (content != null) {
                        messageAction = interactionHook.sendMessage(content)
                                .addEmbeds(embeds);
                    } else {
                        messageAction = interactionHook.sendMessageEmbeds(embeds);
                    }
                    if (BotPermissionUtil.canWrite(channel, Permission.MESSAGE_ATTACH_FILES)) {
                        if (fileAttachmentMap.size() > 0) {
                            for (String fileName : fileAttachmentMap.keySet()) {
                                messageAction = messageAction.addFiles(FileUpload.fromData(fileAttachmentMap.get(fileName), fileName));
                            }
                        }
                    }
                    messageAction = messageAction.setAllowedMentions(allowedMentions);
                    action = messageAction.addComponents(actionRows);
                } else {
                    WebhookMessageCreateAction<Message> messageAction;
                    InteractionHook interactionHook = commandEvent.getButtonInteractionEvent().getHook();
                    if (content != null) {
                        messageAction = interactionHook.sendMessage(content)
                                .addEmbeds(embeds);
                    } else {
                        messageAction = interactionHook.sendMessageEmbeds(embeds);
                    }
                    if (BotPermissionUtil.canWrite(channel, Permission.MESSAGE_ATTACH_FILES)) {
                        if (fileAttachmentMap.size() > 0) {
                            for (String fileName : fileAttachmentMap.keySet()) {
                                messageAction = messageAction.addFiles(FileUpload.fromData(fileAttachmentMap.get(fileName), fileName));
                            }
                        }
                    }
                    messageAction = messageAction.setAllowedMentions(allowedMentions);
                    action = messageAction.addComponents(actionRows);
                }
            } else {
                if (interactionResponse != null &&
                        interactionResponse.isValid() &&
                        (BotPermissionUtil.canUseExternalEmojisInInteraction(channel) || !getCommandProperties().usesExtEmotes())
                ) {
                    action = interactionResponse.editMessageEmbeds(embeds, actionRows);
                } else {
                    if (content != null) {
                        action = channel.editMessageById(drawMessage.getIdLong(), content)
                                .setEmbeds(embeds)
                                .setComponents(actionRows)
                                .setAllowedMentions(allowedMentions);
                    } else {
                        action = channel.editMessageEmbedsById(drawMessage.getIdLong(), embeds)
                                .setAllowedMentions(allowedMentions)
                                .setComponents(actionRows);
                    }
                }
            }
            // only used for uneditable ephemeral message created with GenericComponentInteractionCreateEvent#reply
            if (!newMessageTest) {
                action.queue(message -> {

                    if (!newMessage) {
                        drawMessage = message;
                    }
                    // changed from  future.complete(message) to  future.complete(drawMessage)
                    future.complete(drawMessage);
                }, e -> {
                    MainLogger.get().error("Draw exception for \"{}\"", getTrigger(), e);
                    future.completeExceptionally(e);
                });
            } else {
                newMessageTest = false;
            }

            return future;
        } finally {
            resetDrawState();
        }
    }

    private synchronized CompletableFuture<Message> drawMessage(PrivateChannel channel, String content, EmbedBuilder eb, boolean newMessage) {
        try {
            CompletableFuture<Message> future = new CompletableFuture<>();
            ArrayList<MessageEmbed> embeds = new ArrayList<>();
            try {
                if (eb != null) {
                    embeds.add(eb.build());
                }
                embeds.addAll(additionalEmbeds);
            } catch (Throwable e) {
                StringBuilder sb = new StringBuilder("Embed exception with fields:");
                if (eb != null) {
                    eb.getFields().forEach(field -> sb
                            .append("\nKey: \"")
                            .append(field.getName())
                            .append("\"; Value: \"")
                            .append(field.getValue())
                            .append("\"")
                    );
                }
                MainLogger.get().error(sb.toString(), e);
                throw e;
            }

            if (actionRows == null) {
                actionRows = Collections.emptyList();
            }

            HashSet<String> usedIds = new HashSet<>();
            for (ActionRow actionRow : actionRows) {
                for (ActionComponent component : actionRow.getActionComponents()) {
                    if (component.getId() != null) {
                        if (usedIds.contains(component.getId())) {
                            future.completeExceptionally(new Exception("Duplicate custom id \"" + component.getId() + "\""));
                            return future;
                        }
                        usedIds.add(component.getId());
                    }
                }
            }

            RestAction<Message> action;
            if (drawMessage == null || newMessage) {
                if (commandEvent.isGuildMessageReceivedEvent()) {
                    MessageCreateAction messageAction;
                    Message message = commandEvent.getMessageReceivedEvent().getMessage();
                    if (content != null) {
                        messageAction = JDAUtil.replyMessage(message, content)
                                .setEmbeds(embeds);
                    } else {
                        messageAction = JDAUtil.replyMessageEmbeds(true, message, embeds);
                    }
                    if (fileAttachmentMap.size() > 0) {
                        for (String fileName : fileAttachmentMap.keySet()) {
                            messageAction = messageAction.addFiles(FileUpload.fromData(fileAttachmentMap.get(fileName), fileName));
                        }
                    }
                    messageAction = messageAction.setAllowedMentions(allowedMentions);
                    action = messageAction.setComponents(actionRows);
                } else if (commandEvent.isSlashCommandInteractionEvent()) {
                    WebhookMessageCreateAction<Message> messageAction;
                    InteractionHook interactionHook = commandEvent.getSlashCommandInteractionEvent().getHook();
                    if (content != null) {
                        messageAction = interactionHook.sendMessage(content)
                                .addEmbeds(embeds);
                    } else {
                        messageAction = interactionHook.sendMessageEmbeds(embeds);
                    }
                    if (fileAttachmentMap.size() > 0) {
                        for (String fileName : fileAttachmentMap.keySet()) {
                            messageAction = messageAction.addFiles(FileUpload.fromData(fileAttachmentMap.get(fileName), fileName));
                        }
                    }
                    messageAction = messageAction.setAllowedMentions(allowedMentions);
                    action = messageAction.addComponents(actionRows);
                } else {
                    WebhookMessageCreateAction<Message> messageAction;
                    InteractionHook interactionHook = commandEvent.getButtonInteractionEvent().getHook();
                    if (content != null) {
                        messageAction = interactionHook.sendMessage(content)
                                .addEmbeds(embeds);
                    } else {
                        messageAction = interactionHook.sendMessageEmbeds(embeds);
                    }
                    if (fileAttachmentMap.size() > 0) {
                        for (String fileName : fileAttachmentMap.keySet()) {
                            messageAction = messageAction.addFiles(FileUpload.fromData(fileAttachmentMap.get(fileName), fileName));
                        }
                    }
                    messageAction = messageAction.setAllowedMentions(allowedMentions);
                    action = messageAction.addComponents(actionRows);
                }
            } else {
                if (interactionResponse != null &&
                        interactionResponse.isValid()
                ) {
                    action = interactionResponse.editMessageEmbeds(embeds, actionRows);
                } else {
                    if (content != null) {
                        action = channel.editMessageById(drawMessage.getIdLong(), content)
                                .setEmbeds(embeds)
                                .setComponents(actionRows)
                                .setAllowedMentions(allowedMentions);
                    } else {
                        action = channel.editMessageEmbedsById(drawMessage.getIdLong(), embeds)
                                .setAllowedMentions(allowedMentions)
                                .setComponents(actionRows);
                    }
                }
            }
            // only used for uneditable ephemeral message created with GenericComponentInteractionCreateEvent#reply
            if (!newMessageTest) {
                action.queue(message -> {

                    if (!newMessage) {
                        drawMessage = message;
                    }
                    // changed from  future.complete(message) to  future.complete(drawMessage)
                    future.complete(drawMessage);
                }, e -> {
                    MainLogger.get().error("Draw exception for \"{}\"", getTrigger(), e);
                    future.completeExceptionally(e);
                });
            } else {
                newMessageTest = false;
            }

            return future;
        } finally {
            resetDrawState();
        }
    }

    public void resetDrawMessage() {
        drawMessage = null;
    }

    public Optional<Message> getDrawMessage() {
        return Optional.ofNullable(drawMessage);
    }

    public void setDrawMessage(Message drawMessage) {
        this.drawMessage = drawMessage;
    }

    public Optional<Long> getDrawMessageId() {
        return getDrawMessage().map(ISnowflake::getIdLong);
    }

    public LogStatus getLogStatus() {
        return logStatus;
    }

    public String getLog() {
        return log;
    }

    public void setLog(LogStatus logStatus, String string) {
        this.log = string;
        this.logStatus = logStatus;
    }

    public void resetDrawState() {
        this.log = "";
        this.logStatus = null;
        this.actionRows = Collections.emptyList();
        this.additionalEmbeds = Collections.emptyList();
        this.fileAttachmentMap.clear();
        this.allowedMentions = MessageRequest.getDefaultMentions();
    }

    public void registerStaticReactionMessage(Message message) {
        DBStaticReactionMessages.getInstance()
                .retrieve(message.getGuild().getIdLong())
                .put(message.getIdLong(), new StaticReactionMessageData(message, getTrigger()));
    }

    public void registerStaticReactionMessage(GuildMessageChannel channel, long messageId) {
        DBStaticReactionMessages.getInstance()
                .retrieve(channel.getGuild().getIdLong())
                .put(messageId, new StaticReactionMessageData(
                        channel.getGuild().getIdLong(),
                        channel.getIdLong(),
                        messageId,
                        getTrigger()
                ));
    }

    public void deregisterListeners() {
        CommandContainer.deregisterListeners(this);
    }

    public synchronized void onListenerTimeOutSuper() throws Throwable {
        if (canHaveTimeOut) {
            canHaveTimeOut = false;
            onListenerTimeOut();
        }
    }

    protected void onListenerTimeOut() throws Throwable {
    }

    public String getString(String key, String... args) {
        String text = TextManager.getString(locale, category, commandProperties.trigger() + "_" + key, args);
        if (prefix != null) text = text.replace("{PREFIX}", prefix);
        return text;
    }

    public String getString(String key, int option, String... args) {
        String text = TextManager.getString(locale, category, commandProperties.trigger() + "_" + key, option, args);
        if (prefix != null) text = text.replace("{PREFIX}", prefix);
        return text;
    }

    public String getString(String key, boolean secondOption, String... args) {
        String text = TextManager.getString(locale, category, commandProperties.trigger() + "_" + key, secondOption, args);
        if (prefix != null) text = text.replace("{PREFIX}", prefix);
        return text;
    }

    public Permission[] getAdjustedUserGuildPermissions() {
        return commandProperties.userGuildPermissions();
    }

    public Function<Member, Boolean> getCustomUserGuildPermissions() {
        return m -> false;
    }

    public Permission[] getAdjustedUserChannelPermissions() {
        Permission[] permissions = commandProperties.userChannelPermissions();
        return processUserPermissions(permissions);
    }

    public Permission[] getUserPermissions() {
        List<Permission> permissionList = new ArrayList<>(Arrays.asList(getCommandProperties().userGuildPermissions()));
        permissionList.addAll(Arrays.asList(getCommandProperties().userChannelPermissions()));
        return permissionList.toArray(new Permission[0]);
    }

    private Permission[] processUserPermissions(Permission[] permissions) {
        if (Arrays.stream(permissions).anyMatch(permission -> permission == Permission.ADMINISTRATOR)) {
            return new Permission[]{Permission.ADMINISTRATOR};
        }

        if ((this instanceof OnReactionListener || this instanceof OnStaticReactionAddListener || this instanceof OnStaticReactionRemoveListener) &&
                Arrays.stream(permissions).noneMatch(permission -> permission == Permission.MESSAGE_HISTORY)
        ) {
            permissions = Arrays.copyOf(permissions, permissions.length + 1);
            permissions[permissions.length - 1] = Permission.MESSAGE_HISTORY;
        }

        return permissions;
    }

    public boolean isModCommand() {
        return Arrays.stream(commandProperties.userGuildPermissions()).anyMatch(p -> p != Permission.MESSAGE_HISTORY) ||
                Arrays.stream(commandProperties.userChannelPermissions()).anyMatch(p -> p != Permission.MESSAGE_HISTORY);
    }

    public Permission[] getAdjustedBotGuildPermissions() {
        return commandProperties.botGuildPermissions();
    }

    public Permission[] getAdjustedBotChannelPermissions() {
        Permission[] permissions = commandProperties.botChannelPermissions();
        return processBotPermissions(permissions);
    }

    private Permission[] processBotPermissions(Permission[] permissions) {
        if (Arrays.stream(permissions).anyMatch(permission -> permission == Permission.ADMINISTRATOR)) {
            return new Permission[]{Permission.ADMINISTRATOR};
        }

        if (this instanceof OnReactionListener || this instanceof OnStaticReactionAddListener || this instanceof OnStaticReactionRemoveListener) {
            if (Arrays.stream(permissions).noneMatch(permission -> permission == Permission.VIEW_CHANNEL)) {
                permissions = Arrays.copyOf(permissions, permissions.length + 1);
                permissions[permissions.length - 1] = Permission.VIEW_CHANNEL;
            }
            if (Arrays.stream(permissions).noneMatch(permission -> permission == Permission.MESSAGE_HISTORY)) {
                permissions = Arrays.copyOf(permissions, permissions.length + 1);
                permissions[permissions.length - 1] = Permission.MESSAGE_HISTORY;
            }
            if (Arrays.stream(permissions).noneMatch(permission -> permission == Permission.MESSAGE_ADD_REACTION)) {
                permissions = Arrays.copyOf(permissions, permissions.length + 1);
                permissions[permissions.length - 1] = Permission.MESSAGE_ADD_REACTION;
            }
        }

        if (this instanceof OnMessageInputListener) {
            if (Arrays.stream(permissions).noneMatch(permission -> permission == Permission.VIEW_CHANNEL)) {
                permissions = Arrays.copyOf(permissions, permissions.length + 1);
                permissions[permissions.length - 1] = Permission.VIEW_CHANNEL;
            }
        }

        if (commandProperties.requiresEmbeds()) {
            permissions = Arrays.copyOf(permissions, permissions.length + 1);
            permissions[permissions.length - 1] = Permission.MESSAGE_EMBED_LINKS;
        }

        return permissions;
    }

    public boolean canRunOnGuild(long guildId, long userId) {
        long[] allowedGuildIds = commandProperties.exclusiveGuilds();
        long[] allowedUserIds = commandProperties.exclusiveUsers();

        return ((allowedGuildIds.length == 0) || Arrays.stream(allowedGuildIds).anyMatch(checkGuildId -> checkGuildId == guildId)) &&
                ((allowedUserIds.length == 0) || Arrays.stream(allowedUserIds).anyMatch(checkUserId -> checkUserId == userId)) &&
                (!commandProperties.onlyPublicVersion() || Program.publicVersion());
    }

    public Optional<LocalDate> getReleaseDate() {
        int[] releaseDateArray = commandProperties.releaseDate();
        return Optional.ofNullable(releaseDateArray.length == 3 ? LocalDate.of(releaseDateArray[0], releaseDateArray[1], releaseDateArray[2]) : null);
    }

    public void addCompletedListener(Runnable runnable) {
        completedListeners.add(runnable);
    }

    public List<Runnable> getCompletedListeners() {
        return Collections.unmodifiableList(completedListeners);
    }

    public long getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getTrigger() {
        return getCommandProperties().trigger();
    }

    public JSONObject getAttachments() {
        return attachments;
    }

    public void setAtomicAssets(GuildMessageChannel guildMessageChannel, Member member) {
        atomicGuild = new AtomicGuild(guildMessageChannel.getGuild());
        atomicGuildMessageChannel = new AtomicGuildMessageChannel(guildMessageChannel);
        atomicMember = new AtomicMember(member);
        memberEffectiveName = member.getEffectiveName();
        memberMention = member.getAsMention();
        memberEffectiveAvatarUrl = member.getUser().getEffectiveAvatarUrl();
        memberTag = member.getUser().getAsTag();
    }

    public CommandEvent getCommandEvent() {
        return commandEvent;
    }

    public void setCommandEvent(CommandEvent commandEvent) {
        this.commandEvent = commandEvent;
    }

    public InteractionResponse getInteractionResponse() {
        return interactionResponse;
    }

    public void setInteractionResponse(InteractionResponse interactionResponse) {
        this.interactionResponse = interactionResponse;
    }

    public Optional<Guild> getGuild() {
        return Optional.ofNullable(atomicGuild)
                .flatMap(AtomicGuild::get);
    }

    public Optional<GuildMessageChannel> getGuildMessageChannel() {
        return Optional.ofNullable(atomicGuildMessageChannel)
                .flatMap(AtomicGuildMessageChannel::get);
    }

    public Optional<Member> getMember() {
        return Optional.ofNullable(atomicMember)
                .flatMap(AtomicMember::get);
    }

    public Optional<String> getMemberEffectiveName() {
        return Optional.ofNullable(memberEffectiveName);
    }

    public Optional<String> getMemberAsMention() {
        return Optional.ofNullable(memberMention);
    }

    public Optional<String> getMemberEffectiveAvatarUrl() {
        return Optional.ofNullable(memberEffectiveAvatarUrl);
    }

    public Optional<String> getMemberAsTag() {
        return Optional.ofNullable(memberTag);
    }

    public Optional<Long> getGuildId() {
        return Optional.ofNullable(atomicGuild)
                .map(AtomicGuild::getIdLong);
    }

    public Optional<Long> getTextChannelId() {
        return Optional.ofNullable(atomicGuildMessageChannel)
                .map(AtomicGuildMessageChannel::getIdLong);
    }

    public Optional<Long> getMemberId() {
        return Optional.ofNullable(atomicMember)
                .map(AtomicMember::getIdLong);
    }

    public void setNewMessageTest(boolean newMessageTest) {
        this.newMessageTest = newMessageTest;
    }

}
