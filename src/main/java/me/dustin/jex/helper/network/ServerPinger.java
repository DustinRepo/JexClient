package me.dustin.jex.helper.network;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.helper.misc.ChatHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public class ServerPinger {

    private static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final String ip;

    public ServerPinger(String ip) {
        this.ip = ip;
    }

    public void pingServer() {
        new Thread(() -> {
            ServerAddress serverAddress = ServerAddress.parseString(ip);
            Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
            optional.ifPresent(this::ping);
        }).start();
    }

    private void ping(InetSocketAddress inetAddress) {
        final Connection clientConnection = Connection.connectToServer(inetAddress, false);
        clientConnection.setListener(new ClientStatusPacketListener() {
            @Override
            public void handleStatusResponse(ClientboundStatusResponsePacket packet) {
                ServerStatus serverMetadata = packet.getStatus();
                //description
                ChatHelper.INSTANCE.addRawMessage(ChatFormatting.GRAY + "----------------");
                ChatHelper.INSTANCE.addRawMessage(serverMetadata.getDescription());
                ChatHelper.INSTANCE.addRawMessage(ChatFormatting.GRAY + "----------------");

                //version info
                if (serverMetadata.getVersion() != null)
                    ChatHelper.INSTANCE.addRawMessage("Version: " + ChatFormatting.AQUA + serverMetadata.getVersion().getName() + ChatFormatting.WHITE + " (Protocol ver: " + ChatFormatting.AQUA + serverMetadata.getVersion().getProtocol() + ChatFormatting.WHITE + ")");
                //players
                if (serverMetadata.getPlayers() != null) {
                    ChatHelper.INSTANCE.addRawMessage("Players online: " + serverMetadata.getPlayers().getNumPlayers() + "/" + serverMetadata.getPlayers().getMaxPlayers());

                    for (GameProfile gameProfile : serverMetadata.getPlayers().getSample()) {
                        if (gameProfile.getId().compareTo(emptyUUID) != 0)
                            ChatHelper.INSTANCE.addRawMessage(ChatFormatting.GREEN + gameProfile.getName());
                    }
                }
            }

            @Override
            public void handlePongResponse(ClientboundPongResponsePacket packet) {
                long pingTime = System.currentTimeMillis() - packet.getTime();
                ChatHelper.INSTANCE.addRawMessage("Ping: " + pingTime);
            }

            @Override
            public void onDisconnect(Component reason) {
                ChatHelper.INSTANCE.addRawMessage("Disconnected: " + reason);
            }

            @Override
            public Connection getConnection() {
                return clientConnection;
            }
        });
        clientConnection.send(new ClientIntentionPacket(inetAddress.getAddress().getHostAddress(), inetAddress.getPort(), ConnectionProtocol.STATUS));
        clientConnection.send(new ServerboundStatusRequestPacket());
        clientConnection.send(new ServerboundPingRequestPacket(System.currentTimeMillis()));
    }
}
