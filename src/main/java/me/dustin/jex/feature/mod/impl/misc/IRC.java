package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRenderChatHud;
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

@Feature.Manifest(category = Feature.Category.MISC, description = "Connect to an IRC server to chat with other Jex users", visible = false)
public class IRC extends Feature {

    @Op(name = "Send Prefix", maxStringLength = 2)
    public String sendPrefix = "@";

    private boolean drawButtons = true;
    public boolean ircChatOverride;
    public boolean renderAboveChat = true;
    public static ChatHud ircChatHud = new ChatHud(Wrapper.INSTANCE.getMinecraft());

    private static ArrayList<String> messageList = new ArrayList<>();

    public IRCManager ircManager = new IRCManager(Wrapper.INSTANCE.getMinecraft().getSession().getUsername());

    @EventPointer
    private final EventListener<EventSendMessage> eventSendMessageEventListener = new EventListener<>(event -> {
        if (event.getMessage().startsWith(sendPrefix) || ircChatOverride) {
            event.cancel();
            String message = event.getMessage().startsWith(sendPrefix) ? event.getMessage().substring(sendPrefix.length()) : event.getMessage();
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
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!messageList.isEmpty()) {
            for (String s : messageList) {
                ChatHelper.INSTANCE.addRawMessage(s);
            }
            messageList.clear();
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRenderChatHud> eventRenderChatHudEventListener = new EventListener<>(event -> {
        if (event.getChatHud() == Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud()) {
            if (ircChatOverride) {
                event.cancel();
                ircChatHud.render(event.getMatrixStack(), event.getTickDelta());
            }
        }
    });

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        IChatScreen iChatScreen = (IChatScreen) event.getScreen();
        String chatString = iChatScreen.getText();
        if (renderAboveChat) {
            FontHelper.INSTANCE.drawWithShadow(event.getMatrixStack(), "\2477Selected channel: " + (ircChatOverride ? "\247cIRC" : "\247rGame Chat"), iChatScreen.getWidget().x + 84, iChatScreen.getWidget().y - 11, ColorHelper.INSTANCE.getClientColor());
        }
        if (chatString.startsWith(sendPrefix) || ircChatOverride) {
            int color = 0xffFF5555;
            int users = ircManager.getUsers(IRCManager.IRC_ChannelName).length;
            String usersString = "IRC Users: \247f" + users;
            String nameString = "Name: \247f" + ircManager.getNick();
            Render2DHelper.INSTANCE.fillAndBorder(event.getMatrixStack(), iChatScreen.getWidget().x - 2, iChatScreen.getWidget().y - 2, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y + iChatScreen.getWidget().getHeight() - 2, color, 0x00ffffff, 1);

            //IRC info right side
            Render2DHelper.INSTANCE.fill(event.getMatrixStack(), (iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(nameString) - 4, iChatScreen.getWidget().y - 13, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y - 2, 0x90000000);
            FontHelper.INSTANCE.drawWithShadow(event.getMatrixStack(), nameString, ((iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(nameString)) - 1.5f, iChatScreen.getWidget().y - 11, color);
            Render2DHelper.INSTANCE.fill(event.getMatrixStack(), (iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(usersString) - 4, iChatScreen.getWidget().y - 24, iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth() - 2, iChatScreen.getWidget().y - 13, 0x90000000);
            FontHelper.INSTANCE.drawWithShadow(event.getMatrixStack(), usersString, ((iChatScreen.getWidget().x + iChatScreen.getWidget().getWidth()) - FontHelper.INSTANCE.getStringWidth(usersString)) - 1.5f, iChatScreen.getWidget().y - 22, color);
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, ChatScreen.class));

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
