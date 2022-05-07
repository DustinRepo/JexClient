package me.dustin.jex.load.mixin.minecraft;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

@Mixin(ServerList.class)
public class MixinServerList {

    @Shadow @Final private List<ServerData> serverList;

    @Inject(method = "load", at = @At("RETURN"))
    public void loadFile1(CallbackInfo ci) {
        //TODO: maybe add this back at some point
        for (ServerData server : this.serverList) {
            if (server.ip.equalsIgnoreCase("play.jexclient.com"))
                return;
        }
        this.serverList.add(new ServerData("Jex Anarchy Server", "play.jexclient.com", false));
    }

}
