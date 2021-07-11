package me.dustin.jex.feature.mod.impl.misc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

import java.util.Objects;

@Feature.Manifest(name = "DiscordRPC", category = Feature.Category.MISC, description = "Show on Discord that you are using Jex Client")
public class Discord extends Feature {

    @Op(name = "Show Server IP")
    public boolean showServerIP = true;

    private DiscordRPC lib;
    private Thread discordThread;
    private DiscordRichPresence presence;
    private final String APPID = "812897399550246914";

    public Discord() {
        this.setState(true);
    }

    @Override
    public void onEnable() {
        lib = DiscordRPC.INSTANCE;
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> JexClient.INSTANCE.getLogger().info("Ready!");
        lib.Discord_Initialize(APPID, handlers, true, "");
        presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000; // epoch second
        presence.largeImageKey = "jex";
        presence.largeImageText = "Jex Client " + JexClient.INSTANCE.getVersion();
        presence.details = "Jex Client " + JexClient.INSTANCE.getVersion();
        presence.state = getDetails();
        lib.Discord_UpdatePresence(presence);
        // in a worker thread
        (discordThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (presence == null)return;
                presence.details = "Jex Client " + JexClient.INSTANCE.getVersion();
                presence.state = getDetails();
                lib.Discord_UpdatePresence(presence);
                //lib.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }, "RPC-Callback-Handler")).start();
    }

    @Override
    public void onDisable() {
        if (discordThread != null && !discordThread.isInterrupted()) {
            discordThread.interrupt();
        }
        if (lib != null) {
            lib.Discord_Shutdown();
            lib = null;
            presence = null;
        }
    }

    private String getDetails() {
        StringBuilder sb = new StringBuilder();
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            sb.append("In the menus");
        else {
            sb.append("Playing ");
            if (showServerIP) {
                sb.append(Wrapper.INSTANCE.getMinecraft().isIntegratedServerRunning() ? "SinglePlayer" : Objects.requireNonNull(Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry()).address + " " + Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().version.getString());
            } else {
                sb.append(Wrapper.INSTANCE.getMinecraft().isIntegratedServerRunning() ? "SinglePlayer" : "Mineman Multiplayer");
            }
        }
        return sb.toString();
    }
}
