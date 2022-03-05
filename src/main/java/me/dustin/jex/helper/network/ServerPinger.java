package me.dustin.jex.helper.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class ServerPinger {

    public static byte PACKET_HANDSHAKE = 0x00, PACKET_STATUSREQUEST = 0x00, PACKET_PING = 0x01;
    public static int PROTOCOL_VERSION = 758;
    public static int STATUS_PING = 1;

    private static UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private String ip;

    public ServerPinger(String ip) {
        this.ip = ip;
    }

    public void pingServer() {
        new Thread(() -> {
            ServerAddress serverAddress = ServerAddress.parse(ip);
            Optional<InetSocketAddress> optional = AllowedAddressResolver.DEFAULT.resolve(serverAddress).map(Address::getInetSocketAddress);
            if (optional.isPresent()) {
                // \/ this one uses code much more intwined with Minecraft, I just thought it would be fun to re-create the packet sending and receiving myself
                //ping(optional.get());
                try {
                    InetAddress inetAddress = InetAddress.getByName(serverAddress.getAddress());
                    ChatHelper.INSTANCE.addClientMessage("Resolved IP: " + inetAddress.getHostAddress());
                    Socket socket = new Socket(inetAddress.getHostAddress(), serverAddress.getPort());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    sendHandshake(dataOutputStream, serverAddress, NetworkState.STATUS);
                    sendQueryRequest(dataOutputStream);

                    int size = readVarInt(dataInputStream);//even tho we don't use it currently you MUST read this in order to not mess up the order of others being read
                    int packetID = readVarInt(dataInputStream);

                    if (packetID == 0x00) {
                        String resp = receiveQueryRequest(dataInputStream);
                        JsonObject mainObject = JsonHelper.INSTANCE.gson.fromJson(resp, JsonObject.class);
                        JexClient.INSTANCE.getLogger().info(resp);

                        //server desc
                        JsonObject description = mainObject.getAsJsonObject("description");
                        Text text = Text.Serializer.fromJson(description);
                        ChatHelper.INSTANCE.addRawMessage(Formatting.GRAY + "----------------");
                        ChatHelper.INSTANCE.addRawMessage(text);
                        ChatHelper.INSTANCE.addRawMessage(Formatting.GRAY + "----------------");

                        //server info
                        JsonObject version = mainObject.getAsJsonObject("version");
                        String verName = version.get("name").getAsString();
                        int verProtocol = version.get("protocol").getAsInt();
                        ChatHelper.INSTANCE.addRawMessage("Version: " + Formatting.AQUA + verName + Formatting.WHITE + " (Protocol ver: " + Formatting.AQUA + verProtocol + Formatting.WHITE + ")");

                        //player list
                        JsonObject playersObject = mainObject.getAsJsonObject("players");
                        JsonArray jsonArray = playersObject.getAsJsonArray("sample");
                        int maxPlayers = playersObject.get("max").getAsInt();
                        int onlinePlayers = playersObject.get("online").getAsInt();

                        ChatHelper.INSTANCE.addRawMessage("Players online: " + onlinePlayers + "/" + maxPlayers);
                        if (jsonArray != null)
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JsonObject playerObj = jsonArray.get(i).getAsJsonObject();
                                String name = playerObj.get("name").getAsString();
                                String uuid = playerObj.get("id").getAsString();
                                ChatHelper.INSTANCE.addRawMessage(Formatting.GREEN + name);
                            }
                    } else
                        ChatHelper.INSTANCE.addClientMessage("Server sent bad packet");

                    sendPing(dataOutputStream);
                    size = readVarInt(dataInputStream);
                    packetID = readVarInt(dataInputStream);

                    if (packetID == 0x01)
                        receivePing(dataInputStream);
                    else
                        ChatHelper.INSTANCE.addClientMessage("Server sent bad packet");

                    //testing doing a simple login, for some reason after the LoginStart packet the server doesn't respond then says we timed out after ~10 seconds
                    /*socket = new Socket(inetAddress.getHostAddress(), serverAddress.getPort());
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    sendHandshake(dataOutputStream, serverAddress, NetworkState.LOGIN);
                    sendLoginStart(dataOutputStream);

                    size = readVarInt(dataInputStream);
                    packetID = readVarInt(dataInputStream);

                    if (packetID == 0x01)
                        receiveEncryptionRequest(size, dataInputStream);
                    else
                        ChatHelper.INSTANCE.addClientMessage("Server sent bad packet");*/


                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void sendHandshake(DataOutputStream dataOutputStream, ServerAddress serverAddress, NetworkState networkState) {
        try {
            ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
            DataOutputStream handshakePacket = new DataOutputStream(handshakeBytes);
            handshakePacket.writeByte(PACKET_HANDSHAKE);//packet id
            writeVarInt(handshakePacket, PROTOCOL_VERSION);//protocol version
            writeVarInt(handshakePacket, serverAddress.getAddress().length());//length of address
            handshakePacket.writeBytes(serverAddress.getAddress());//address
            handshakePacket.writeShort(serverAddress.getPort());//port
            writeVarInt(handshakePacket, networkState.getId());//status id

            writeVarInt(dataOutputStream, handshakeBytes.size());//size of data
            dataOutputStream.write(handshakeBytes.toByteArray());//data
            handshakeBytes.close();
            handshakePacket.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    private void sendQueryRequest(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeByte(0x01);//size of data
            dataOutputStream.writeByte(PACKET_STATUSREQUEST);//packet id
        } catch (Exception e) {e.printStackTrace();}
    }

    private String receiveQueryRequest(DataInputStream dataInputStream) {
        try {
            int strLength = readVarInt(dataInputStream);
            byte[] strBytes = new byte[strLength];
            dataInputStream.readFully(strBytes);
            return new String(strBytes);
        } catch (Exception e) {e.printStackTrace();}
        return "null";
    }

    private void sendPing(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeByte(0x09);//size of packet id + data - 1 byte for packet id and 8 bytes for a long
            dataOutputStream.writeByte(PACKET_PING);//packet id
            dataOutputStream.writeLong(System.currentTimeMillis());
        } catch (Exception e) {e.printStackTrace();}
    }

    private void receivePing(DataInputStream dataInputStream) {
        try {
            byte[] bytes = new byte[8];//size of data - 8 bytes for a long
            dataInputStream.readFully(bytes);

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.put(bytes);
            buffer.flip();//need flip
            long startTime = buffer.getLong();
            long pingTime = System.currentTimeMillis() - startTime;
            ChatHelper.INSTANCE.addRawMessage("Ping: " + pingTime);
        } catch (Exception e) {e.printStackTrace();}
    }

    private void sendLoginStart(DataOutputStream dataOutputStream) {
        try {//this appears to send properly, but afer ~10 seconds the connection then times out on the server
            //maybe I have to send another packet after the LoginStart packet before listening for the EncyptionRequest packet?
            ByteArrayOutputStream loginBytes = new ByteArrayOutputStream();
            DataOutputStream loginPacket = new DataOutputStream(loginBytes);

            writeVarInt(loginPacket, 0x00);//packet id

            String name = "Herobrine";
            writeVarInt(loginPacket, name.length());
            loginPacket.writeBytes(name);

            dataOutputStream.writeByte(loginBytes.size() + 1);//size of data + packet id
            dataOutputStream.write(loginBytes.toByteArray());

            loginBytes.close();
            loginPacket.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    private void receiveEncryptionRequest(int size, DataInputStream dataInputStream) {
        size = size - 1;//remove the byte for packet ID
        try {
            String serverID = "";
            int publicKeyLength;
            byte[] publicKey;
            int verifyTokenLength;
            byte[] verifyToken;

            //server id
            int strSize = dataInputStream.readInt();
            byte[] strBytes = new byte[strSize];
            dataInputStream.readFully(strBytes, 0, strSize);

            //public key
            publicKeyLength = dataInputStream.readInt();
            publicKey = new byte[publicKeyLength];
            dataInputStream.readFully(publicKey, 0, size);

            //verifyToken
            verifyTokenLength = dataInputStream.readInt();
            verifyToken = new byte[verifyTokenLength];
            dataInputStream.readFully(verifyToken, 0, size);

            JexClient.INSTANCE.getLogger().info(serverID + " " + publicKey.length + " " + verifyToken.length);
        } catch (Exception e) {e.printStackTrace();}
    }

    public static int readVarInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        while (true) {
            int k = in.readByte();

            i |= (k & 0x7F) << j++ * 7;

            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }

            if ((k & 0x80) != 128) {
                break;
            }
        }

        return i;
    }

    public static void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.write(paramInt);
                return;
            }

            out.write(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    private void ping(InetSocketAddress inetAddress) {
        final ClientConnection clientConnection = ClientConnection.connect(inetAddress, false);
        clientConnection.setPacketListener(new ClientQueryPacketListener() {
            @Override
            public void onResponse(QueryResponseS2CPacket packet) {
                ServerMetadata serverMetadata = packet.getServerMetadata();
                ChatHelper.INSTANCE.addRawMessage(serverMetadata.getDescription().getString());
                if (serverMetadata.getPlayers() != null) {
                    ChatHelper.INSTANCE.addRawMessage("Players: ");
                    for (GameProfile gameProfile : serverMetadata.getPlayers().getSample()) {
                        if (gameProfile.getId().compareTo(emptyUUID) != 0)
                            ChatHelper.INSTANCE.addRawMessage(Formatting.DARK_GRAY + gameProfile.getName());
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
