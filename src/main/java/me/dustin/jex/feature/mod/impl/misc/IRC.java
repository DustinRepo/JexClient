package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRenderChatHud;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.irc.IRCManager;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.load.impl.IChatScreen;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.LiteralText;
import org.jibble.pircbot.User;

import java.util.ArrayList;
import java.util.StringJoiner;

@Feature.Manifest(name = "IRC", category = Feature.Category.MISC, description = "Connect to an IRC server to chat with other Jex users", visible = false)
public class IRC extends Feature {

    @Op(name = "Send Prefix", maxStringLength = 2)
    public String sendPrefix = "@";

    private boolean drawButtons = true;
    public boolean ircChatOverride;
    public static ChatHud ircChatHud = new ChatHud(Wrapper.INSTANCE.getMinecraft());

    private static ArrayList<String> messageList = new ArrayList<>();

    public IRCManager ircManager = new IRCManager(Wrapper.INSTANCE.getMinecraft().getSession().getUsername());

    @EventListener(events = {EventSendMessage.class, EventPlayerPackets.class, EventRenderChatHud.class, EventDrawScreen.class})
    private void runMethod(Event event) {
        if (ircManager.isConnected() && event instanceof EventSendMessage eventSendMessage) {
            if (eventSendMessage.getMessage().startsWith(sendPrefix) || ircChatOverride) {
                eventSendMessage.cancel();
                String message = eventSendMessage.getMessage().startsWith(sendPrefix) ? eventSendMessage.getMessage().substring(sendPrefix.length()) : eventSendMessage.getMessage();
                if (message.isEmpty()) {
                    ChatHelper.INSTANCE.addRawMessage("\2477[\247aIRC\2477]: \247fYour message was empty");
                    return;
                }
                if (message.equalsIgnoreCase("list")) {
                    StringJoiner stringJoiner = new StringJoiner(", ");
                    for (User user : ircManager.getUsers(IRCManager.IRC_ChannelName)) {
                        stringJoiner.add(user.getNick());
                    }
                    ChatHelper.INSTANCE.addClientMessage("Users: " + stringJoiner);
                    ircChatHud.addMessage(new LiteralText("Users: " + stringJoiner));
                    return;
                }
                ircManager.sendMessage(IRCManager.IRC_ChannelName, message);

                String ircString = "\2477[\247aIRC\2477][\247cYou\2477]: \247f" + message;
                ircChatHud.addMessage(new LiteralText(ircString));
                ChatHelper.INSTANCE.addRawMessage(ircString);
            }
        } else if (ircManager.isConnected() && event instanceof EventDrawScreen eventDrawScreen && eventDrawScreen.getScreen() instanceof ChatScreen chatScreen) {
            if (eventDrawScreen.getMode() == EventDrawScreen.Mode.POST) {
                IChatScreen iChatScreen = (IChatScreen) chatScreen;
                String chatString = iChatScreen.getText();
                if (!chatString.isEmpty() && !chatString.startsWith(CommandManagerJex.INSTANCE.getPrefix()) && !chatString.startsWith(sendPrefix)) {
                    FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), "\2477Selected channel: " + (ircChatOverride ? "\247cIRC" : "\247rGame Chat"),iChatScreen.getWidget().x - 2, iChatScreen.getWidget().y - 11, ColorHelper.INSTANCE.getClientColor());
                }
                if (chatString.isEmpty()) {
                    FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), "\2477Selected channel: " + (ircChatOverride ? "\247cIRC" : "\247rGame Chat"), iChatScreen.getWidget().x + 84, iChatScreen.getWidget().y - 11, ColorHelper.INSTANCE.getClientColor());
                }
                if (chatString.startsWith(sendPrefix) || ircChatOverride) {
                    int color = 0xffFF5555;
                    int users = ircManager.getUsers(IRCManager.IRC_ChannelName).length;
                    String usersString = "IRC Users: \247f" + users;
                    String nameString = "Name: \247f" + ircManager.getNick();
                    Render2DHelper.INSTANCE.fillAndBorder(eventDrawScreen.getMatrixStack(), iChatScreen.getWidget().x - 2, iChatScreen.getWidget().y - 2, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y + iChatScreen.getWidget().getHeight() - 2, color, 0x00ffffff, 1);

                    //IRC info right side
                    Render2DHelper.INSTANCE.fill(eventDrawScreen.getMatrixStack(), (iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(nameString) - 4, iChatScreen.getWidget().y - 13, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y - 2, 0x90000000);
                    FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), nameString, ((iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(nameString)) - 1.5f, iChatScreen.getWidget().y - 11, color);
                    Render2DHelper.INSTANCE.fill(eventDrawScreen.getMatrixStack(), (iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(usersString) - 4, iChatScreen.getWidget().y - 24, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y - 13, 0x90000000);
                    FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), usersString, ((iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(usersString)) - 1.5f, iChatScreen.getWidget().y - 22, color);
                }
            }
        } else if (ircManager.isConnected() && event instanceof EventPlayerPackets eventPlayerPackets && eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!messageList.isEmpty()) {
                for (String s : messageList) {
                    ChatHelper.INSTANCE.addRawMessage(s);
                }
                messageList.clear();
            }
        } else if (event instanceof EventRenderChatHud eventRenderChatHud && eventRenderChatHud.getChatHud() == Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud()) {
            if (ircChatOverride) {
                eventRenderChatHud.cancel();
                ircChatHud.render(eventRenderChatHud.getMatrixStack(), eventRenderChatHud.getTickDelta());
            }
        }
    }

    public static void addIRCMessage(String sender, String message) {
        if (message.isEmpty())
            return;
        String ircString = "\2477[\247aIRC\2477][\247c" + sender + "\2477]: \247f" + message;
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            ChatHelper.INSTANCE.addRawMessage(ircString);
        } else {
            messageList.add(ircString);
        }
        ircChatHud.addMessage(new LiteralText(ircString));
    }

    @Override
    public void onEnable() {
        ircManager.connect();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ircManager.disconnect();
        super.onDisable();
    }
}
