package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.irc.IRCClient;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderChatHud;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.load.impl.IChatScreen;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import java.util.StringJoiner;
import java.util.function.Consumer;

@Feature.Manifest(category = Feature.Category.MISC, description = "Connect to an IRC server to chat with other Jex users", enabled = true)
public class IRC extends Feature {

    @Op(name = "Send Prefix", maxStringLength = 2)
    public String sendPrefix = "@";

    public boolean ircChatOverride;
    public boolean renderAboveChat = true;
    public static ChatComponent ircChatHud = new ChatComponent(Wrapper.INSTANCE.getMinecraft());

    public IRCClient ircClient;

    private final Consumer<String> messageListener = IRC::addIRCMessage;
    private final Consumer<String> disconnectListener = reason -> {
        addIRCMessage("Disconnected: " + reason);
        ircChatOverride = false;
        ircClient = null;
    };


    @Override
    public void onEnable() {
        ircClient = new IRCClient(Wrapper.INSTANCE.getMinecraft().getUser().getName());
        ircClient.setMessageConsumer(messageListener);
        ircClient.setDisconnectConsumer(disconnectListener);
        ircClient.connect("132.145.154.217", 6969);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (ircClient != null)
            ircClient.disconnect();
        super.onDisable();
    }

    @EventPointer
    private final EventListener<EventSendMessage> eventSendMessageEventListener = new EventListener<>(event -> {
        if ((event.getMessage().startsWith(sendPrefix) || ircChatOverride) && ircClient != null) {
            event.cancel();
            String message = event.getMessage().startsWith(sendPrefix) ? event.getMessage().substring(sendPrefix.length()) : event.getMessage();
            if (message.isEmpty()) {
                addIRCMessage("Your message was empty.");
                return;
            }
            if (message.equalsIgnoreCase("list")) {
                StringJoiner stringJoiner = new StringJoiner(", ");
                for (String user : ircClient.getUsers()) {
                    stringJoiner.add(user);
                }
                addIRCMessage("Users: " + stringJoiner);
                return;
            }
            if (message.startsWith("/login")) {
                if (message.split(" ").length > 2) {
                    String username = message.split(" ")[1];
                    String password = message.split(" ")[2];
                    ircClient.adminLogin(username, password);
                } else {
                    addIRCMessage("Invalid args. /login <username> <password>");
                }
                return;
            }
            if (message.startsWith("/ban")) {
                if (message.split(" ").length > 2) {
                    String username = message.split(" ")[1];
                    String reason = message.replace("/ban " + username + " ", "");
                    ircClient.ban(username, reason);
                } else {
                    addIRCMessage("Invalid args. /ban <name> <reason>");
                }
                return;
            }

            if (message.startsWith("/kick")) {
                if (message.split(" ").length > 2) {
                    String username = message.split(" ")[1];
                    String reason = message.replace("/kick " + username + " ", "");
                    ircClient.kick(username, reason);
                } else {
                    addIRCMessage("Invalid args. /kick <name> <reason>");
                }
                return;
            }

            if (message.startsWith("/mute")) {
                if (message.split(" ").length > 1) {
                    String username = message.split(" ")[1];
                    String timeString = message.split(" ")[2];
                    String reason = message.replace("/mute " + username + " " + timeString + " ", "");
                    ircClient.mute(username, reason, parseMuteTime(timeString));
                } else {
                    addIRCMessage("Invalid args. /admin <name>");
                }
                return;
            }

            if (message.startsWith("/admin")) {
                if (message.split(" ").length > 1) {
                    String username = message.split(" ")[1];
                    ircClient.admin(username);
                } else {
                    addIRCMessage("Invalid args. /admin <name>");
                }
                return;
            }
            //don't bother adding IRC message since the server will send us a message packet back
            ircClient.sendMessage(message);
        }
    });

    @EventPointer
    private final EventListener<EventRenderChatHud> eventRenderChatHudEventListener = new EventListener<>(event -> {
        if (event.getChatHud() == Wrapper.INSTANCE.getMinecraft().gui.getChat()) {
            if (ircChatOverride) {
                event.cancel();
                ircChatHud.render(event.getPoseStack(), event.getTickDelta());
            }
        }
    });

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        IChatScreen iChatScreen = (IChatScreen) event.getScreen();
        String chatString = iChatScreen.getText();
        if (ircClient != null && ircClient.isConnected() && renderAboveChat) {
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), "\2477Selected channel: " + (ircChatOverride ? "\247cIRC" : "\247rGame Chat"), iChatScreen.getWidget().x + 84, iChatScreen.getWidget().y - 11, ColorHelper.INSTANCE.getClientColor());
        }
        if ((chatString.startsWith(sendPrefix) || ircChatOverride) && ircClient != null && ircClient.isConnected()) {
            int color = 0xffFF5555;
            int users = ircClient.getUsers().length;
            String usersString = "IRC Users: \247f" + users;
            String nameString = "Name: \247f" + ircClient.getUsername();
            Render2DHelper.INSTANCE.fillAndBorder(event.getPoseStack(), iChatScreen.getWidget().x - 2, iChatScreen.getWidget().y - 2, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y + iChatScreen.getWidget().getHeight() - 2, color, 0x00ffffff, 1);

            //IRC info right side
            Render2DHelper.INSTANCE.fill(event.getPoseStack(), (iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(nameString) - 4, iChatScreen.getWidget().y - 13, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y - 2, 0x90000000);
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), nameString, ((iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(nameString)) - 1.5f, iChatScreen.getWidget().y - 11, color);
            Render2DHelper.INSTANCE.fill(event.getPoseStack(), (iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(usersString) - 4, iChatScreen.getWidget().y - 24, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y - 13, 0x90000000);
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), usersString, ((iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(usersString)) - 1.5f, iChatScreen.getWidget().y - 22, color);
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, ChatScreen.class));

    private long parseMuteTime(String s) {
        long time = 0;
        try {
            if (s.length() >= 2) {
                char aChar = s.toLowerCase().charAt(0);
                char tChar = s.toLowerCase().charAt(1);
                int amount = Integer.parseInt(String.valueOf(aChar));
                if (tChar == 'd')
                    time += (24L * 60L * 60L) * (1000L * amount);
                else if (tChar == 'h')
                    time += (60L * 60L) * (1000L * amount);
                else if (tChar == 'm')
                    time += 60L * (1000L * amount);
                else if (tChar == 's')
                    time += (1000L * amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (s.length() > 2) {
            s = s.substring(2);
            time += parseMuteTime(s);
        }
        return time;
    }

    public static void addIRCMessage(String message) {
        if (message.isEmpty())
            return;
        String ircString = "\2478[\247cIRC\2478] \2477" + message;
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            ChatHelper.INSTANCE.addRawMessage(ircString);
        }
        ircChatHud.addMessage(Component.nullToEmpty(ircString));
    }
}
