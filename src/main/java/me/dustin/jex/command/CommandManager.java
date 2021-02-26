package me.dustin.jex.command;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.command.core.Command;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IChatScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import org.reflections.Reflections;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public enum CommandManager {

    INSTANCE;
    private static ArrayList<Command> commands = new ArrayList<>();
    private String prefix = ".";
    private boolean foundCommand;
    private int overlayAlpha = 0;
    private boolean overlayOn = false;
    public void init() {
        Reflections reflections = new Reflections("me.dustin.jex", new org.reflections.scanners.Scanner[0]);

        Set<Class<? extends Command>> allClasses = reflections.getSubTypesOf(Command.class);
        allClasses.forEach(clazz -> {
            try {
                Command instance = clazz.newInstance();
                this.getCommands().add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        EventAPI.getInstance().register(this);
    }

    @EventListener(events = {EventSendMessage.class})
    public void run(EventSendMessage eventSendMessage) {
        if (eventSendMessage.getMessage().startsWith(prefix)) {
            foundCommand = false;
            commands.forEach(command -> {
                String[] args = eventSendMessage.getMessage().split(" ");
                if (command.getName().equalsIgnoreCase(args[0].replace(prefix, "")) || command.getAlias().contains(args[0].replace(prefix, "").toLowerCase())) {
                    command.runCommand(eventSendMessage.getMessage(), args);
                    foundCommand = true;
                }
            });
            if (!foundCommand) {
                ChatHelper.INSTANCE.addClientMessage("Command not found. Use " + prefix + "help for a list of commands.");
            }
            eventSendMessage.cancel();
        }

    }
    @EventListener(events = {EventDrawScreen.class, EventTick.class})
    private void runOtherMethod(Event event) {
        if (event instanceof EventDrawScreen) {
            EventDrawScreen eventDrawScreen = (EventDrawScreen)event;
            if (eventDrawScreen.getScreen() instanceof ChatScreen) {
                if (eventDrawScreen.getMode() == EventDrawScreen.Mode.POST) {
                    IChatScreen chatScreen = (IChatScreen) (ChatScreen) eventDrawScreen.getScreen();
                    if (chatScreen.getText().startsWith(this.getPrefix())) {
                        overlayOn = true;
                    } else {
                        overlayOn = false;
                    }
                    Color color1 = Color.decode("0x" + Integer.toHexString(ColorHelper.INSTANCE.getClientColor()).substring(2));
                    int color = new Color(color1.getRed(), color1.getGreen(), color1.getBlue(), overlayAlpha).getRGB();
                    Render2DHelper.INSTANCE.fillAndBorder(((EventDrawScreen) event).getMatrixStack(), chatScreen.getWidget().x - 2, chatScreen.getWidget().y - 2, chatScreen.getWidget().x + chatScreen.getWidget().getWidth() - 2, chatScreen.getWidget().y + chatScreen.getWidget().getHeight() - 2, color, 0x00ffffff, 1);
                    if (overlayOn) {
                        String chatLine = chatScreen.getText();
                        String cmd = chatLine.contains(" ") ? chatLine.split(" ")[0] : chatLine;
                        cmd = cmd.replace(getPrefix(), "");
                        int possibleCount = 0;
                        if (!cmd.isEmpty()) {
                            for (Command command : getPossibleCommands(cmd)) {
                                String name = command.getName();
                                if (!command.getAlias().isEmpty()) {
                                    name += " \247f| \247r";
                                    for (String s : command.getAlias()) {
                                        name += s + (s.equalsIgnoreCase(command.getAlias().get(command.getAlias().size() - 1)) ? " " : "\2477, \247r");
                                    }
                                }
                                name = name.trim() + " \247f| \2477" + command.getSyntax() + " \247f| \2477" + command.getDescription();
                                Render2DHelper.INSTANCE.fill(eventDrawScreen.getMatrixStack(), chatScreen.getWidget().x - 2, chatScreen.getWidget().y - 12 - (10 * possibleCount), chatScreen.getWidget().x + 2 + FontHelper.INSTANCE.getStringWidth(name), chatScreen.getWidget().y - 2 - (10 * possibleCount), 0xaa000000);
                                FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), name, chatScreen.getWidget().x, chatScreen.getWidget().y - 11 - (10 * possibleCount), ColorHelper.INSTANCE.getClientColor());
                                possibleCount++;
                            }
                        }
                    }
                }
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
                    overlayAlpha+=25;
                }
            } else {
                if (overlayAlpha > 0) {
                    overlayAlpha-=25;
                }
            }
            if (overlayAlpha > 255)
                overlayAlpha = 255;
            if (overlayAlpha < 0)
                overlayAlpha = 0;
        }
    }

    public Command getCommand(String command) {
        for (Command command1 : commands) {
            if (command1.getName().equalsIgnoreCase(command) || command1.getAlias().contains(command))
                return command1;
        }
        return null;
    }
    public ArrayList<Command> getPossibleCommands(String s) {
        ArrayList<Command> commands = new ArrayList<>();
        for (Command command1 : this.getCommands()) {
            if (command1.getName().toLowerCase().startsWith(s.toLowerCase()) || command1.getAlias().contains(s.toLowerCase()))
                commands.add(command1);
        }
        return commands;
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
