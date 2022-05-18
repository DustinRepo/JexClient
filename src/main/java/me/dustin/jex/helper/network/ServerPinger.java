package me.dustin.jex.helper.network;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.helper.misc.ChatHelper;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
            ServerAddress serverAddress = ServerAddress.parse(ip);
            Optional<InetSocketAddress> optional = AllowedAddressResolver.DEFAULT.resolve(serverAddress).map(Address::getInetSocketAddress);
            optional.ifPresent(this::ping);
        }).start();
    }

    private void ping(InetSocketAddress inetAddress) {
        final ClientConnection clientConnection = ClientConnection.connect(inetAddress, false);
        clientConnection.setPacketListener(new ClientQueryPacketListener() {
            @Override
            public void onResponse(QueryResponseS2CPacket packet) {
                ServerMetadata serverMetadata = packet.getServerMetadata();
                //description
                ChatHelper.INSTANCE.addRawMessage(Formatting.GRAY + "----------------");
                ChatHelper.INSTANCE.addRawMessage(serverMetadata.getDescription());
                ChatHelper.INSTANCE.addRawMessage(Formatting.GRAY + "----------------");

                //version info
                if (serverMetadata.getVersion() != null)
                    ChatHelper.INSTANCE.addRawMessage("Version: " + Formatting.AQUA + serverMetadata.getVersion().getGameVersion() + Formatting.WHITE + " (Protocol ver: " + Formatting.AQUA + serverMetadata.getVersion().getProtocolVersion() + Formatting.WHITE + ")");
                //players
                if (serverMetadata.getPlayers() != null) {
                    ChatHelper.INSTANCE.addRawMessage("Players online: " + serverMetadata.getPlayers().getOnlinePlayerCount() + "/" + serverMetadata.getPlayers().getPlayerLimit());

                    for (GameProfile gameProfile : serverMetadata.getPlayers().getSample()) {
                        if (gameProfile.getId().compareTo(emptyUUID) != 0)
                            ChatHelper.INSTANCE.addRawMessage(Formatting.GREEN + gameProfile.getName());
                    }
                }
            }

            @Override
            public void onPong(QueryPongS2CPacket packet) {
                long pingTime = System.currentTimeMillis() - packet.getStartTime();
                ChatHelper.INSTANCE.addRawMessage("Ping: " + pingTime);
            }

            @Override
            public void onDisconnected(Text reason) {
                ChatHelper.INSTANCE.addRawMessage("Disconnected: " + reason);
            }

            @Override
            public ClientConnection getConnection() {
                return clientConnection;
            }
        });
        clientConnection.send(new HandshakeC2SPacket(inetAddress.getAddress().getHostAddress(), inetAddress.getPort(), NetworkState.STATUS));
        clientConnection.send(new QueryRequestC2SPacket());
        clientConnection.send(new QueryPingC2SPacket(System.currentTimeMillis()));
    }
}
