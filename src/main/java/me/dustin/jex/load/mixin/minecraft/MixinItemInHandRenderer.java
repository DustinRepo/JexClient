package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventRenderHeldItem;
import me.dustin.jex.event.render.EventVisualCooldown;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Shadow private float oMainHandHeight;

    @Shadow private float mainHandHeight;

    @Shadow private float offHandHeight;

    @Shadow private float oOffHandHeight;

    @Shadow private ItemStack mainHandItem;

    @Shadow @Final private Minecraft minecraft;

    @Shadow private ItemStack offHandItem;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    public void renderFirstPersonItem(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        EventRenderHeldItem eventRenderHeldItem = new EventRenderHeldItem(item, hand, tickDelta, matrices).run();
        if (eventRenderHeldItem.isCancelled())
            ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void updateHeldItems1(CallbackInfo ci) {
        if (((EventVisualCooldown)new EventVisualCooldown().run()).isCancelled()) {
            ci.cancel();
            this.oMainHandHeight = this.mainHandHeight;
            this.oOffHandHeight = this.offHandHeight;
            LocalPlayer clientPlayerEntity = this.minecraft.player;
            ItemStack itemStack = clientPlayerEntity.getMainHandItem();
            ItemStack itemStack2 = clientPlayerEntity.getOffhandItem();
            if (ItemStack.matches(this.mainHandItem, itemStack)) {
                this.mainHandItem = itemStack;
            }

            if (ItemStack.matches(this.offHandItem, itemStack2)) {
                this.offHandItem = itemStack2;
            }

            if (clientPlayerEntity.isHandsBusy()) {
                this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
                this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
            } else {
                this.mainHandHeight += Mth.clamp((this.mainHandItem == itemStack ? 1 : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
                this.offHandHeight += Mth.clamp((float)(this.offHandItem == itemStack2 ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
            }

            if (this.mainHandHeight < 0.1F) {
                this.mainHandItem = itemStack;
            }

            if (this.offHandHeight < 0.1F) {
                this.offHandItem = itemStack2;
            }
        }
    }
}
