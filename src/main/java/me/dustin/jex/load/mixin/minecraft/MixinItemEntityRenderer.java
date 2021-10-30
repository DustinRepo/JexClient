package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.JexClient;
import me.dustin.jex.event.render.EventRotateItemEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class MixinItemEntityRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "java/util/Random.setSeed(J)V"))
    public void renderItemEntity(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        new EventRotateItemEntity(itemEntity, matrixStack, g).run();
    }

}
