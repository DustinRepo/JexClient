package me.dustin.jex.helper.entity;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public class FakePlayerEntity extends AbstractClientPlayer {

    public FakePlayerEntity(ClientLevel world, GameProfile profile) {
        super(world, profile, null);
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public ResourceLocation getSkinTextureLocation() {
        if (isSkinLoaded())
            return super.getSkinTextureLocation();
        else
            return MCAPIHelper.INSTANCE.getPlayerSkin(this == Freecam.playerEntity ? Wrapper.INSTANCE.getMinecraft().getUser().getGameProfile().getId() : this.uuid);
    }
}
