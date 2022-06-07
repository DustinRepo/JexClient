package me.dustin.jex.load.mixin.minecraft;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.addon.Addon;
import me.dustin.jex.addon.cape.Cape;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinAbstractClientPlayer extends PlayerEntity {

    private final Identifier jex_cape = new Identifier("jex", "cape/jex_cape.png");

    public MixinAbstractClientPlayer(World world, BlockPos pos, float yaw, GameProfile profile, PlayerPublicKey playerPublicKey) {
        super(world, pos, yaw, profile, playerPublicKey);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void constructor(ClientWorld clientWorld_1, GameProfile gameProfile_1, PlayerPublicKey playerPublicKey, CallbackInfo ci) {
        Addon.loadAddons((AbstractClientPlayerEntity) (Object) this);
    }

    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    public void getCapeTexture(CallbackInfoReturnable<Identifier> cir) {
        String uuid = ((AbstractClientPlayerEntity) (Object) this).getUuidAsString().replace("-", "");
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
