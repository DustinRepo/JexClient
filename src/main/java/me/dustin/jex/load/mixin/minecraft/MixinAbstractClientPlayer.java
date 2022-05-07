package me.dustin.jex.load.mixin.minecraft;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.addon.Addon;
import me.dustin.jex.addon.cape.Cape;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends Player {

    private final ResourceLocation jex_cape = new ResourceLocation("jex", "cape/jex_cape.png");

    public MixinAbstractClientPlayer(Level world, BlockPos pos, float yaw, GameProfile profile, ProfilePublicKey playerPublicKey) {
        super(world, pos, yaw, profile, playerPublicKey);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void constructor(ClientLevel clientWorld_1, GameProfile gameProfile_1, ProfilePublicKey playerPublicKey, CallbackInfo ci) {
        Addon.loadAddons((AbstractClientPlayer) (Object) this);
    }

    @Inject(method = "getCloakTextureLocation", at = @At("HEAD"), cancellable = true)
    public void getCapeTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        String uuid = ((AbstractClientPlayer) (Object) this).getStringUUID().replace("-", "");
        if (Cape.capes.containsKey(uuid)) {
            cir.setReturnValue(Cape.capes.get(uuid));
        } else if (this.getGameProfile() == Wrapper.INSTANCE.getLocalPlayer().getGameProfile()) {
            if (Cape.capes.containsKey("self")) {
                cir.setReturnValue(Cape.capes.get("self"));
            }
        } else if (Addon.isLinkedToAccount(uuid)) {
            cir.setReturnValue(jex_cape);
        }
    }

}
