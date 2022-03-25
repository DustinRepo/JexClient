package me.dustin.jex.helper.network;

import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.thealtening.TheAlteningHelper;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.util.Session;
import net.minecraft.network.Packet;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum NetworkHelper {
    INSTANCE;

    private YggdrasilMinecraftSessionService storedSessionService;
    private Session storedSession;

    public void sendPacket(Packet<?> packet) {
        try {
            if (Wrapper.INSTANCE.getLocalPlayer() != null)
                Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacketDirect(Packet<?> packet) {
        Wrapper.INSTANCE.getLocalPlayer().networkHandler.getConnection().send(packet);
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
                this.storedSessionService = (YggdrasilMinecraftSessionService)Wrapper.INSTANCE.getMinecraft().getSessionService();
                Wrapper.INSTANCE.getIMinecraft().setSessionService(TheAlteningHelper.INSTANCE.getTheAlteningSessionService());
            }
        }
    }

    public YggdrasilMinecraftSessionService getStoredSessionService() {
        return storedSessionService;
    }

    public Session getStoredSession() {
        return storedSession;
    }

    public void setStoredSession(Session storedSession) {
        this.storedSession = storedSession;
    }

    public void disconnect(String reason, String message) {
        boolean bl = Wrapper.INSTANCE.getMinecraft().isInSingleplayer();
        boolean bl2 = Wrapper.INSTANCE.getMinecraft().isConnectedToRealms();
        Wrapper.INSTANCE.getWorld().disconnect();
        if (bl) {
            Wrapper.INSTANCE.getMinecraft().disconnect(new DisconnectedScreen(new TitleScreen(), new LiteralText("Disconnect"), new TranslatableText("menu.savingLevel")));
        } else {
            Wrapper.INSTANCE.getMinecraft().disconnect();
        }

        TitleScreen titleScreen = new TitleScreen();
        if (bl) {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(titleScreen, Text.of(reason), Text.of(message)));
        } else if (bl2) {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(new RealmsMainScreen(titleScreen), Text.of(reason), Text.of(message)));
        } else {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(new MultiplayerScreen(titleScreen), Text.of(reason), Text.of(message)));
        }

    }

    public enum SessionService {
        MOJANG, THEALTENING;
    }
}
