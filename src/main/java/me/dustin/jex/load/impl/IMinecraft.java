package me.dustin.jex.load.impl;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Session;

import java.net.Proxy;

public interface IMinecraft {

    void setSession(Session session);

    void setRightClickDelayTimer(int timer);

    int getFPS();
    BufferBuilderStorage getBufferBuilderStorage();

    RenderTickCounter getRenderTickCounter();

    void setProxy(Proxy proxy);
}
