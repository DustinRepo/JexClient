package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventRenderHeldItem;
import me.dustin.jex.event.render.EventVisualCooldown;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {
    @Shadow private float equipProgressMainHand;

    @Shadow private float equipProgressOffHand;

    @Shadow private ItemStack mainHand;

    @Shadow private ItemStack offHand;

    @Shadow private float prevEquipProgressMainHand;

    @Shadow private float prevEquipProgressOffHand;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    public void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EventRenderHeldItem eventRenderHeldItem = new EventRenderHeldItem(item, hand, tickDelta, matrices).run();
        if (eventRenderHeldItem.isCancelled())
            ci.cancel();
    }

    @Inject(method = "updateHeldItems", at = @At("HEAD"), cancellable = true)
    public void updateHeldItems1(CallbackInfo ci) {
        if (((EventVisualCooldown)new EventVisualCooldown().run()).isCancelled()) {
            ci.cancel();
            this.prevEquipProgressMainHand = this.equipProgressMainHand;
            this.prevEquipProgressOffHand = this.equipProgressOffHand;
            ClientPlayerEntity clientPlayerEntity = this.client.player;
            ItemStack itemStack = clientPlayerEntity.getMainHandStack();
            ItemStack itemStack2 = clientPlayerEntity.getOffHandStack();
            if (ItemStack.areEqual(this.mainHand, itemStack)) {
                this.mainHand = itemStack;
            }

            if (ItemStack.areEqual(this.offHand, itemStack2)) {
                this.offHand = itemStack2;
            }

            if (clientPlayerEntity.isRiding()) {
                this.equipProgressMainHand = MathHelper.clamp(this.equipProgressMainHand - 0.4F, 0.0F, 1.0F);
                this.equipProgressOffHand = MathHelper.clamp(this.equipProgressOffHand - 0.4F, 0.0F, 1.0F);
            } else {//                                                                             \/ This part here is what changed, it used to calculate with attack progress
                this.equipProgressMainHand += MathHelper.clamp((this.mainHand == itemStack ? 1 : 0.0F) - this.equipProgressMainHand, -0.4F, 0.4F);
                this.equipProgressOffHand += MathHelper.clamp((float)(this.offHand == itemStack2 ? 1 : 0) - this.equipProgressOffHand, -0.4F, 0.4F);
            }

            if (this.equipProgressMainHand < 0.1F) {
                this.mainHand = itemStack;
            }

            if (this.equipProgressOffHand < 0.1F) {
                this.offHand = itemStack2;
            }
        }
    }
}
