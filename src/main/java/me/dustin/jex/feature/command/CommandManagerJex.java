package me.dustin.jex.feature.command;
/*
 * @Author Dustin
 * 9/29/2019
 */

import com.mojang.brigadier.CommandDispatcher;
import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IChatScreen;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.CommandSuggestor;
import org.reflections.Reflections;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public enum CommandManagerJex {

    INSTANCE;
    private String prefix = ".";
    private int overlayAlpha = 0;
    private boolean overlayOn = false;

    public CommandSuggestor jexCommandSuggestor;

    private static ArrayList<Command> commands = new ArrayList<>();
    public static final CommandDispatcher<FabricClientCommandSource> DISPATCHER = new CommandDispatcher<>();

    public void registerCommands() {
        EventAPI.getInstance().unregister(this);
        this.getCommands().clear();
        Reflections reflections = new Reflections("me.dustin.jex.feature.command.impl");
        Set<Class<? extends Command>> allClasses = reflections.getSubTypesOf(Command.class);
        allClasses.forEach(clazz -> {
            try {
                @SuppressWarnings("deprecation")
                Command instance = clazz.newInstance();
                instance.registerCommand();
                this.getCommands().add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        this.getCommands().sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        EventAPI.getInstance().register(this);
    }

    @EventListener(events = {EventDrawScreen.class, EventTick.class, EventSendMessage.class})
    private void runOtherMethod(Event event) {
        if (event instanceof EventSendMessage eventSendMessage) {
            if (eventSendMessage.getMessage().startsWith(prefix) && ClientCommandInternals.executeCommand(eventSendMessage.getMessage())) {
                eventSendMessage.cancel();
            }
        }
        if (event instanceof EventDrawScreen eventDrawScreen && eventDrawScreen.getMode() == EventDrawScreen.Mode.POST) {
            if (eventDrawScreen.getScreen() instanceof ChatScreen) {
                IChatScreen chatScreen = (IChatScreen) (ChatScreen) eventDrawScreen.getScreen();
                if (chatScreen.getText().startsWith(this.getPrefix())) {
                    overlayOn = true;
                    chatScreen.getWidget().setMaxLength(100000);
                } else {
                    overlayOn = false;
                    chatScreen.getWidget().setMaxLength(256);
                }
                Color color1 = Color.decode("0x" + Integer.toHexString(ColorHelper.INSTANCE.getClientColor()).substring(2));
                int color = new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), overlayAlpha).getRGB();
                Render2DHelper.INSTANCE.fillAndBorder(((EventDrawScreen) event).getMatrixStack(), chatScreen.getWidget().x - 2, chatScreen.getWidget().y - 2, chatScreen.getWidget().x + chatScreen.getWidget().getWidth() - 2, chatScreen.getWidget().y + chatScreen.getWidget().getHeight() - 2, color, 0x00ffffff, 1);
            } else {
                overlayOn = false;
                overlayAlpha = 0;
            }
        }
        if (event instanceof EventTick) {
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
        }
    }

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
