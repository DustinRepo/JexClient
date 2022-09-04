package me.dustin.jex.load.impl;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;

public interface IMinecraft {

    void setSession(Session session);

    void setRightClickDelayTimer(int timer);

    void setSessionService(MinecraftSessionService sessionService);

    void setProfileKeys(ProfileKeys profileKeys);

    UserApiService getUserApiService();
    YggdrasilAuthenticationService getAuthenticationService();
}
