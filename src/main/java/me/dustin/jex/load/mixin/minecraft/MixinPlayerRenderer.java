package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.addon.hat.HatFeatureRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer extends LivingEntityRenderer<Player, PlayerModel<Player>> {

	public MixinPlayerRenderer(EntityRendererProvider.Context ctx, PlayerModel<Player> model, float shadowRadius) {
		super(ctx, model, shadowRadius);
	}

	@Inject(method = { "<init>" }, at = { @At("RETURN") })
	private void construct(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
		addLayer(new HatFeatureRenderer((RenderLayerParent<Player, PlayerModel<Player>>) (Object) this));
	}

}
