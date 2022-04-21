package me.dustin.jex.helper.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.NetworkState;
import net.minecraft.util.Formatting;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

public enum ServerScrubber {
    INSTANCE;

    private boolean isSearching;
    private boolean found;

    private final ArrayList<String> servers = new ArrayList<>();
    private final ArrayList<Thread> threads = new ArrayList<>();

    public void searchFor(String name) {
        if (isSearching) {
            ChatHelper.INSTANCE.addClientMessage("Error! already searching for a player. Please wait");
            return;
        }
        EventManager.register(this);
        servers.forEach(ip1 -> {
            Thread thread = new Thread(() -> {
                int port = 25565;
                String ip = ip1;
                if (ip.contains(":")) {
                    port = Integer.parseInt(ip.split(":")[1]);
                    ip = ip.split(":")[0];
                }

                JexClient.INSTANCE.getLogger().info("Searching " + ip + " for player");
                try {
                    isSearching = true;
                    MinecraftServerAddress minecraftServerAddress = MinecraftServerAddress.resolve(ip, port);
                    if (minecraftServerAddress == null) {
                        System.out.println("NULL: " + ip + ":" + port);
                        return;
                    }
                    Socket socket = new Socket(minecraftServerAddress.getIp(), minecraftServerAddress.getPort());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    sendHandshake(dataOutputStream, minecraftServerAddress);
                    sendQueryRequest(dataOutputStream);

                    int size = readVarInt(dataInputStream);//even tho we don't use it currently you MUST read this in order to not mess up the order of others being read
                    int packetID = readVarInt(dataInputStream);

                    if (packetID != 0x00) {
                        JexClient.INSTANCE.getLogger().info("bad packet from" + minecraftServerAddress.getIp() + ":" + minecraftServerAddress.getPort());
                        return;
                    }

                    String resp = receiveQueryRequest(dataInputStream);
                    JsonObject mainObject = JsonHelper.INSTANCE.gson.fromJson(resp, JsonObject.class);

                    JsonObject playersObject = mainObject.getAsJsonObject("players");
                    JsonArray jsonArray = playersObject.getAsJsonArray("sample");
                    if (jsonArray != null)
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonObject playerObj = jsonArray.get(i).getAsJsonObject();
                            String playerName = playerObj.get("name").getAsString();
                            if (playerName.equalsIgnoreCase(name)) {//we found him
                                ChatHelper.INSTANCE.addClientMessage(Formatting.GOLD + playerName + Formatting.GRAY + " found on server: " + Formatting.AQUA + minecraftServerAddress.getIp() + ":" + minecraftServerAddress.getPort());
                                found = true;
                                socket.close();
                            }
                        }
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads.add(thread);
            thread.start();
        });
    }

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (isSearching) {
            if (found) {
                threads.forEach(Thread::interrupt);
                threads.clear();
                found = false;
                isSearching = false;
                EventManager.unregister(this);
                return;
            }
            boolean runningThread = false;
            for (Thread thread : threads) {
                if (thread.isAlive() && !thread.isInterrupted())
                    runningThread = true;
            }
            if (!runningThread) {
                ChatHelper.INSTANCE.addClientMessage("Player not found.");
                isSearching = false;
                threads.clear();
                EventManager.unregister(this);
            }
        }
    }, new TickFilter(EventTick.Mode.PRE));

    private void sendHandshake(DataOutputStream dataOutputStream, MinecraftServerAddress serverAddress) {
        try {
            ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
            DataOutputStream handshakePacket = new DataOutputStream(handshakeBytes);
            handshakePacket.writeByte(0x00);//packet id
            writeVarInt(handshakePacket, SharedConstants.getProtocolVersion());//protocol version
            writeVarInt(handshakePacket, serverAddress.getIp().length());//length of address
            handshakePacket.writeBytes(serverAddress.getIp());//address
            handshakePacket.writeShort(serverAddress.getPort());//port
            writeVarInt(handshakePacket, NetworkState.STATUS.getId());//status id

            writeVarInt(dataOutputStream, handshakeBytes.size());//size of data
            dataOutputStream.write(handshakeBytes.toByteArray());//data
            handshakeBytes.close();
            handshakePacket.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    private void sendQueryRequest(DataOutputStream dataOutputStream) {
        try {
            dataOutputStream.writeByte(0x01);//size of data
            dataOutputStream.writeByte(0x00);//packet id
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



    private void writeVarInt(DataOutputStream out, int paramInt) throws IOException {
        while (true) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.write(paramInt);
                return;
            }

            out.write(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

    private int readVarInt(DataInputStream in) throws IOException {
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

    public void loadDefaultList() {
        servers.add("50kilo.org");
        servers.add("play.snapshotanarchy.net");
        servers.add("anarchycraft.minecraft.best");
        servers.add("hardcoreanarchy.gay");
    }

    public ArrayList<String> getServers() {
        return servers;
    }
}
