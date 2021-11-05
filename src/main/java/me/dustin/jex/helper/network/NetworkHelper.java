package me.dustin.jex.helper.network;

import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.thealtening.TheAlteningHelper;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.network.Packet;
import net.minecraft.text.LiteralText;

public enum NetworkHelper {
    INSTANCE;

    private YggdrasilMinecraftSessionService storedSessionService;

    public void sendPacket(Packet<?> packet) {
        try {
            if (Wrapper.INSTANCE.getLocalPlayer() != null) {
                Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(packet);
            } else {
                Wrapper.INSTANCE.getMinecraft().getNetworkHandler().sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void disconnect(String reason, String message) {
        Wrapper.INSTANCE.getWorld().disconnect();
        Wrapper.INSTANCE.getMinecraft().disconnect();
        Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), new LiteralText(reason), new LiteralText(message)));
    }

    public enum SessionService {
        MOJANG, THEALTENING;
    }
}
