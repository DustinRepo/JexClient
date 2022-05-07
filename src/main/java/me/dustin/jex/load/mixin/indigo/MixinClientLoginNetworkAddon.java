package me.dustin.jex.load.mixin.indigo;

import me.dustin.jex.helper.player.bot.PlayerBot;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.impl.networking.client.ClientLoginNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLoginNetworkAddon.class)
public class MixinClientLoginNetworkAddon {

    @Redirect(method = "handlePacket(ILnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Z", at = @At(value = "INVOKE", target = "net/fabricmc/fabric/api/client/networking/v1/ClientLoginNetworking.registerReceiver (Lnet/minecraft/resources/ResourceLocation;Lnet/fabricmc/fabric/api/client/networking/v1/ClientLoginNetworking$LoginQueryRequestHandler;)Z"))
    public boolean registerRedirect(ResourceLocation id, ClientLoginNetworking.LoginQueryRequestHandler handler) {
        final Connection connection = PlayerBot.currentConnection == null ? ClientNetworkingImpl.getLoginConnection() : PlayerBot.currentConnection;

        if (connection != null) {
            final PacketListener packetListener = connection.getPacketListener();

            if (packetListener instanceof ClientHandshakePacketListenerImpl) {
                return ClientNetworkingImpl.getAddon(((ClientHandshakePacketListenerImpl) packetListener)).registerChannel(id, handler);
            }
        }
        return false;
    }

}
