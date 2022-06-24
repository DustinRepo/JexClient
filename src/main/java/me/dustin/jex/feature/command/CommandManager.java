package me.dustin.jex.feature.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.chat.EventShouldPreviewChat;
import me.dustin.jex.event.chat.EventSortSuggestions;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.plugin.JexPlugin;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ClassHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IChatScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.command.CommandException;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public enum CommandManager {
    INSTANCE;
    private String prefix = ".";
    private int overlayAlpha = 0;
    private boolean overlayOn = false;
    private CommandDispatcher<FabricClientCommandSource> DISPATCHER;
    private static final ArrayList<Command> commands = new ArrayList<>();

    public void initializeCommandManager() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            DISPATCHER = dispatcher;
            EventManager.unregister(this);
            this.getCommands().clear();
            List<Class<?>> classList = ClassHelper.INSTANCE.getClasses("me.dustin.jex.feature.command.impl", Command.class);
            classList.forEach(clazz -> {
                try {
                    @SuppressWarnings("deprecation")
                    Command instance = (Command) clazz.newInstance();
                    this.getCommands().add(instance);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            JexPlugin.commandsLoad();
            this.getCommands().sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
            this.getCommands().forEach(command -> command.registerCommand(dispatcher, dedicated));
            EventManager.register(this);
        });
    }

    @EventPointer
    private final EventListener<EventSendMessage> eventSendMessageEventListener = new EventListener<>(event -> {
        if (executeCommand(event.getMessage()))
            event.cancel();
    });

    @EventPointer
    private final EventListener<EventSortSuggestions> eventSortSuggestionsEventListener = new EventListener<>(event -> {
        boolean isJex = event.getText().startsWith(getPrefix());
        event.getOutput().removeIf(suggestion -> (isJex && !isJexCommand(suggestion.getText()) && !event.getText().contains(" ")) || (!isJex && isJexCommand(suggestion.getText())));
    });

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        IChatScreen chatScreen = (IChatScreen) event.getScreen();
        if (chatScreen.getText().startsWith(this.getPrefix())) {
            overlayOn = true;
            chatScreen.getWidget().setMaxLength(100000);
        } else {
            overlayOn = false;
            chatScreen.getWidget().setMaxLength(256);
        }
        Color color1 = Color.decode("0x" + Integer.toHexString(ColorHelper.INSTANCE.getClientColor()).substring(2));
        int color = new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), overlayAlpha).getRGB();
        Render2DHelper.INSTANCE.fillAndBorder(event.getPoseStack(), chatScreen.getWidget().x - 2, chatScreen.getWidget().y - 2, chatScreen.getWidget().x + chatScreen.getWidget().getWidth() - 2, chatScreen.getWidget().y + chatScreen.getWidget().getHeight() - 2, color, 0x00ffffff, 1);

    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, ChatScreen.class));

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen)) {
            overlayOn = false;
            overlayAlpha = 0;
        }
        if (overlayOn) {
            if (overlayAlpha < 255) {
                overlayAlpha+=35;
            }
        } else {
            if (overlayAlpha > 0) {
                overlayAlpha-=35;
            }
        }
        if (overlayAlpha > 255)
            overlayAlpha = 255;
        if (overlayAlpha < 0)
            overlayAlpha = 0;
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventShouldPreviewChat> eventShouldPreviewChatEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen chatScreen) {
            IChatScreen iChatScreen = (IChatScreen)chatScreen;
            if (iChatScreen.getText().startsWith(prefix)) {
                event.cancel();
                event.setEnabled(false);
            }
        }
    });

    public boolean isJexCommand(String command) {
        if (command.contains(" "))
            command = command.split(" ")[0];
        for (Command brigCommand : commands) {
            if (brigCommand.getName().equalsIgnoreCase(command))
                return true;
            for (String alias : brigCommand.getAlias()) {
                if (alias.equalsIgnoreCase(command))
                    return true;
            }
        }
        return false;
    }

    private boolean executeCommand(String message) {
        if (message.isEmpty() || !message.startsWith(getPrefix()))
            return false;
        FabricClientCommandSource commandSource = (FabricClientCommandSource) Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getCommandSource();
        try {
            DISPATCHER.execute(message.substring(getPrefix().length()), commandSource);
        } catch (CommandSyntaxException e) {
            commandSource.sendError(getErrorMessage(e));
        } catch (CommandException e) {
            commandSource.sendError(e.getTextMessage());
        } catch (RuntimeException e) {
            commandSource.sendError(Text.of(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private Text getErrorMessage(CommandSyntaxException e) {
        Text message = Texts.toText(e.getRawMessage());
        String context = e.getContext();

        return context != null ? Text.translatable("command.context.parse_error", message, context) : message;
    }
    
    public ArrayList<Command> getCommands() {
        return commands;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
