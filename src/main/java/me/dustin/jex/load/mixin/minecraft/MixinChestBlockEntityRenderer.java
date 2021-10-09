package me.dustin.jex.load.mixin.minecraft;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderChest;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntityRenderer.class)
public class MixinChestBlockEntityRenderer {

    @Shadow private boolean christmas;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;FII)V", at = @At("HEAD"), cancellable = true)
    public void render1(MatrixStack matrices, VertexConsumer vertices, ModelPart lid, ModelPart latch, ModelPart base, float openFactor, int light, int overlay, CallbackInfo ci) {
        EventRenderChest eventRenderChest = new EventRenderChest(matrices, EventRenderChest.Mode.PRE, christmas).run();
        this.christmas = eventRenderChest.isChristmas();
        if (eventRenderChest.isCancelled())
            ci.cancel();
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;FII)V", at = @At("RETURN"))
    public void render2(MatrixStack matrices, VertexConsumer vertices, ModelPart lid, ModelPart latch, ModelPart base, float openFactor, int light, int overlay, CallbackInfo ci) {
        EventRenderChest eventRenderChest = new EventRenderChest(matrices, EventRenderChest.Mode.POST, christmas).run();
        this.christmas = eventRenderChest.isChristmas();
    }
}
