package me.dustin.jex.load.impl;

import net.minecraft.client.multiplayer.ClientLevel;

public interface IClientPacketListener {
    void setWorld(ClientLevel world);
}
