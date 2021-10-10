package me.dustin.jex.helper.network;

import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.Packet;

public enum NetworkHelper {
    INSTANCE;

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

}
