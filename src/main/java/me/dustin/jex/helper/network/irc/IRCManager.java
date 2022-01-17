package me.dustin.jex.helper.network.irc;

import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.impl.misc.IRC;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import org.apache.commons.codec.binary.Base64;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IRCManager extends PircBot {

    public static String	IRC_HostName;
    public static int		IRC_HostPort;
    public static String	IRC_ChannelName;

    private String username;

    public IRCManager(String username)
    {
        putNick(username);
    }

    public void connect()
    {
        new Thread(() -> {
            this.setAutoNickChange(true);
            this.setName(username);
            this.changeNick(username);
            try {
                Map<String, String> map = new HashMap<>();
                map.put("uuid", Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""));
                String resp = WebHelper.INSTANCE.sendPOST(new URL(JexClient.INSTANCE.getBaseUrl() + "includes/irc-info.inc.php"), map);
                JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(new String(Base64.decodeBase64(resp)), JsonObject.class);
                IRC_HostName = jsonObject.get("host").getAsString();
                IRC_ChannelName = jsonObject.get("channel").getAsString();
                IRC_HostPort = jsonObject.get("port").getAsInt();
                this.connect(IRC_HostName, IRC_HostPort);
                ChatHelper.INSTANCE.addClientMessage("Connected to IRC host");
                JexClient.INSTANCE.getLogger().info("Connected");
            } catch (IrcException | IOException e) {
                e.printStackTrace();
            }
            this.joinChannel(IRC_ChannelName);
            JexClient.INSTANCE.getLogger().info("Joined channel");
            ChatHelper.INSTANCE.addClientMessage("Joined channel");
        }).start();
    }

    public void putNick(String username) {
        try
        {
            String firstname = username.substring(0, 1);
            int i = Integer.parseInt(firstname);
            JexClient.INSTANCE.getLogger().info("[IRC] Usernames can not begin with numbers");
            username = "MC" + username;
        } catch (NumberFormatException e) {}
        this.username = username;
    }

    @Override
    protected void onDisconnect() {
        ChatHelper.INSTANCE.addClientMessage("IRC disconnected");
        super.onDisconnect();
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        IRC.addIRCMessage(sender, message);
        super.onMessage(channel, sender, login, hostname, message);
    }
}
