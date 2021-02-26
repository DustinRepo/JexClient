package me.dustin.jex.load.mixin;

import me.dustin.jex.addon.hat.HatFeatureRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {


    public MixinPlayerEntityRenderer(EntityRenderDispatcher dispatcher, PlayerEntityModel<PlayerEntity> model, float shadowRadius) {
        super(dispatcher, model, shadowRadius);
    }

    @Inject(method = {"<init>(Lnet/minecraft/client/render/entity/EntityRenderDispatcher;Z)V"}, at = {@At("RETURN")})
    private void construct(EntityRenderDispatcher entityRenderDispatcher, boolean bl, CallbackInfo ci) {
        addFeature(new HatFeatureRenderer((FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>>) (Object) this));
    }

}
