package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventRenderBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {

    @Inject(method = "setupAndRender", at = @At(value = "HEAD"), cancellable = true)
    private static void renderHEAD(BlockEntityRenderer<BlockEntity> renderer, BlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        EventRenderBlockEntity eventRenderBlockEntity = new EventRenderBlockEntity(blockEntity).run();
        if (eventRenderBlockEntity.isCancelled())
            ci.cancel();
    }

}
