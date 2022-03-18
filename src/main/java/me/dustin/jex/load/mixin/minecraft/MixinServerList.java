package me.dustin.jex.load.mixin.minecraft;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerList.class)
public class MixinServerList {

    @Shadow @Final private List<ServerInfo> servers;

    @Inject(method = "loadFile", at = @At("RETURN"))
    public void loadFile1(CallbackInfo ci) {
        //TODO: maybe add this back at some point
        for (ServerInfo server : this.servers) {
            if (server.address.equalsIgnoreCase("play.jexclient.com"))
                return;
        }
        this.servers.add(new ServerInfo("Jex Anarchy Server", "play.jexclient.com", false));
    }

}
