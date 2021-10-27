package me.dustin.jex.helper.network;

import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.Packet;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

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

    public void resetSessionService() {
        if (Wrapper.INSTANCE.getMinecraft() == null)
            return;
        YggdrasilMinecraftSessionService service = (YggdrasilMinecraftSessionService)Wrapper.INSTANCE.getMinecraft().getSessionService();
        setBaseUrl(service, YggdrasilEnvironment.PROD.getEnvironment().getSessionHost() + "/session/minecraft/");
        setJoinUrl(service, YggdrasilEnvironment.PROD.getEnvironment().getSessionHost() + "/session/minecraft/join");
        setCheckUrl(service, YggdrasilEnvironment.PROD.getEnvironment().getSessionHost() + "/session/minecraft/hasJoined");
    }

    public void setBaseUrl(YggdrasilMinecraftSessionService service, String url) {
        try {
            Field field = service.getClass().getDeclaredField("baseUrl");
            field.setAccessible(true);
            field.set(service, url);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setJoinUrl(YggdrasilMinecraftSessionService service, String url) {
        try {
            Field field = service.getClass().getDeclaredField("joinUrl");
            field.setAccessible(true);
            field.set(service, new URL(url));
        } catch (IllegalAccessException | NoSuchFieldException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void setCheckUrl(YggdrasilMinecraftSessionService service, String url) {
        try {
            Field field = service.getClass().getDeclaredField("checkUrl");
            field.setAccessible(true);
            field.set(service, new URL(url));
        } catch (IllegalAccessException | NoSuchFieldException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
