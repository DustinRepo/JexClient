package me.dustin.jex.helper.network;

import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.Packet;

public enum NetworkHelper {
    INSTANCE;

    public void sendPacket(Packet<?> packet) {
        Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(packet);
    }

}
