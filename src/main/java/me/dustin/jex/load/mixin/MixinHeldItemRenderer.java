package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventRenderHeldItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    public void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EventRenderHeldItem eventRenderHeldItem = new EventRenderHeldItem(item, hand, tickDelta, matrices).run();
        if (eventRenderHeldItem.isCancelled())
            ci.cancel();
    }
}
