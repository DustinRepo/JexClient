package me.dustin.jex.load.impl;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.User;

public interface IMinecraft {

    void setSession(User session);

    void setRightClickDelayTimer(int timer);

    void setSessionService(MinecraftSessionService sessionService);
}
