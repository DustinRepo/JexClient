package me.dustin.jex.load.mixin;

import me.dustin.jex.addon.hat.HatFeatureRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
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


    public MixinPlayerEntityRenderer(EntityRendererFactory.Context ctx, PlayerEntityModel<PlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = {"<init>"}, at = {@At("RETURN")})
    private void construct(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        addFeature(new HatFeatureRenderer((FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>>) (Object)this));
    }

}
