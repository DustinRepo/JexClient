package me.dustin.jex.load.mixin.indigo;

import me.dustin.jex.helper.player.bot.PlayerBot;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.impl.networking.client.ClientLoginNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLoginNetworkAddon.class)
public class MixinClientLoginNetworkAddon {
    @Redirect(method = "handlePacket(ILnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Z", at = @At(value = "INVOKE", target = "net/fabricmc/fabric/api/client/networking/v1/ClientLoginNetworking.registerReceiver (Lnet/minecraft/util/Identifier;Lnet/fabricmc/fabric/api/client/networking/v1/ClientLoginNetworking$LoginQueryRequestHandler;)Z"))
    public boolean registerRedirect(Identifier id, ClientLoginNetworking.LoginQueryRequestHandler handler) {
        final ClientConnection connection = PlayerBot.currentConnection == null ? ClientNetworkingImpl.getLoginConnection() : PlayerBot.currentConnection;

        if (connection != null) {
            final PacketListener packetListener = connection.getPacketListener();

            if (packetListener instanceof ClientLoginNetworkHandler) {
                return ClientNetworkingImpl.getAddon(((ClientLoginNetworkHandler) packetListener)).registerChannel(id, handler);
            }
        }
        return false;
    }
}
