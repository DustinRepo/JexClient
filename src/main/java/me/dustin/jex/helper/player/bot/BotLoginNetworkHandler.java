package me.dustin.jex.helper.player.bot;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BotLoginNetworkHandler extends ClientLoginNetworkHandler {
    private final GameProfile gameProfile;
    private final PlayerBot playerBot;
    public BotLoginNetworkHandler(ClientConnection connection, MinecraftClient client, @Nullable Screen parentGui, Consumer<Text> statusConsumer, GameProfile gameProfile, PlayerBot playerBot) {
        super(connection, client, parentGui, statusConsumer);
        this.gameProfile = gameProfile;
        this.playerBot = playerBot;
    }

    @Override
    public void onSuccess(LoginSuccessS2CPacket packet) {
        super.onSuccess(packet);
        this.getConnection().setPacketListener(new BotClientPlayNetworkHandler(Wrapper.INSTANCE.getMinecraft(), null, this.getConnection(), this.gameProfile, Wrapper.INSTANCE.getMinecraft().createTelemetrySender(), playerBot));
        playerBot.setConnected(true);
        if (NetworkHelper.INSTANCE.getStoredSession() != null) {
            Wrapper.INSTANCE.getIMinecraft().setSession(NetworkHelper.INSTANCE.getStoredSession());
            NetworkHelper.INSTANCE.setStoredSession(null);
        }
    }

    @Override
    public void onDisconnected(Text reason) {
        super.onDisconnected(reason);
        playerBot.disconnect();
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " could not connect for reason: " + Formatting.RED + reason.getString());
    }

    @Override
    public void onDisconnect(LoginDisconnectS2CPacket packet) {
        super.onDisconnect(packet);
        playerBot.disconnect();
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " could not connect for reason: " + Formatting.RED + packet.getReason().getString());
    }
}
