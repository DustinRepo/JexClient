package me.dustin.jex.helper.network;

import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.realmsclient.RealmsMainScreen;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.thealtening.TheAlteningHelper;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public enum NetworkHelper {
    INSTANCE;

    private YggdrasilMinecraftSessionService storedSessionService;
    private User storedSession;

    public void sendPacket(Packet<?> packet) {
        try {
            if (Wrapper.INSTANCE.getLocalPlayer() != null)
                Wrapper.INSTANCE.getLocalPlayer().connection.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacketDirect(Packet<?> packet) {
        Wrapper.INSTANCE.getLocalPlayer().connection.getConnection().send(packet);
    }

    public void setSessionService(SessionService sessionService) {
        switch (sessionService) {
            case MOJANG -> {
                if (storedSessionService == null)
                    return;
                Wrapper.INSTANCE.getIMinecraft().setSessionService(storedSessionService);
                storedSessionService = null;
            }
            case THEALTENING -> {
                if (storedSessionService != null)
                    return;
                this.storedSessionService = (YggdrasilMinecraftSessionService)Wrapper.INSTANCE.getMinecraft().getMinecraftSessionService();
                Wrapper.INSTANCE.getIMinecraft().setSessionService(TheAlteningHelper.INSTANCE.getTheAlteningSessionService());
            }
        }
    }

    public YggdrasilMinecraftSessionService getStoredSessionService() {
        return storedSessionService;
    }

    public User getStoredSession() {
        return storedSession;
    }

    public void setStoredSession(User storedSession) {
        this.storedSession = storedSession;
    }

    public void disconnect(String reason, String message) {
        boolean bl = Wrapper.INSTANCE.getMinecraft().isLocalServer();
        boolean bl2 = Wrapper.INSTANCE.getMinecraft().isConnectedToRealms();
        Wrapper.INSTANCE.getWorld().disconnect();
        if (bl) {
            Wrapper.INSTANCE.getMinecraft().clearLevel(new DisconnectedScreen(new TitleScreen(), Component.nullToEmpty("Disconnect"), Component.translatable("menu.savingLevel")));
        } else {
            Wrapper.INSTANCE.getMinecraft().clearLevel();
        }

        TitleScreen titleScreen = new TitleScreen();
        if (bl) {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(titleScreen, Component.nullToEmpty(reason), Component.nullToEmpty(message)));
        } else if (bl2) {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(new RealmsMainScreen(titleScreen), Component.nullToEmpty(reason), Component.nullToEmpty(message)));
        } else {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(titleScreen), Component.nullToEmpty(reason), Component.nullToEmpty(message)));
        }

    }

    public enum SessionService {
        MOJANG, THEALTENING;
    }
}
