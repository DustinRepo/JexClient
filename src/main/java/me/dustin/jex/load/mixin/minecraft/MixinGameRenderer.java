package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.dustin.jex.event.render.*;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {


    @Shadow protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow protected abstract void bobView(PoseStack matrices, float f);

    @Shadow @Final private Camera mainCamera;

    @Shadow public abstract Matrix4f getProjectionMatrix(double d);

    @Shadow public abstract void resetProjectionMatrix(Matrix4f matrix4f);

    @Shadow protected abstract void bobHurt(PoseStack poseStack, float f);

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Nullable private static ShaderInstance rendertypeEntityTranslucentShader;

    @Shadow @Nullable private static ShaderInstance rendertypeGlintDirectShader;

    @Shadow @Nullable private static ShaderInstance rendertypeArmorEntityGlintShader;

    @Shadow @Nullable private static ShaderInstance rendertypeArmorGlintShader;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "com/mojang/blaze3d/systems/RenderSystem.clear(IZ)V"))
    private void onRenderWorld(float partialTicks, long finishTimeNano, PoseStack matrixStack1, CallbackInfo ci) {
        if (Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera == null || Wrapper.INSTANCE.getLocalPlayer() == null)
            return;
        RenderSystem.clearColor(1, 1, 1, 1);
        PoseStack matrixStack = new PoseStack();
        double d = this.getFov(mainCamera, partialTicks, true);
        matrixStack.last().pose().multiply(this.getProjectionMatrix(d));
        resetProjectionMatrix(matrixStack.last().pose());

        this.bobHurt(matrixStack, partialTicks);
        Render3DHelper.INSTANCE.applyCameraRots(matrixStack);
        resetProjectionMatrix(matrixStack.last().pose());
        new EventRender3D.EventRender3DNoBob(matrixStack, partialTicks).run();
        Render3DHelper.INSTANCE.fixCameraRots(matrixStack);
        if (this.minecraft.options.bobView().get()) {
            bobView(matrixStack, partialTicks);
        }
        resetProjectionMatrix(matrixStack.last().pose());

        new EventRender3D(matrixStack1, partialTicks).run();
    }

    @Inject(method = "preloadUiShader", at = @At("RETURN"))
    public void preLoadShaders1(ResourceProvider factory, CallbackInfo ci) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                ShaderHelper.loadCustomMCShaders(manager);
            }

            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation("jex:shaders/core/");
            }
        });
    }

    @Inject(method = "getRendertypeEntityTranslucentShader", at = @At("HEAD"), cancellable = true)
    private static void overrideTranslucentShader(CallbackInfoReturnable<ShaderInstance> cir) {
        EventGetTranslucentShader eventGetTranslucentShader = new EventGetTranslucentShader(rendertypeEntityTranslucentShader).run();
        if (eventGetTranslucentShader.isCancelled())
            cir.setReturnValue(eventGetTranslucentShader.getShader());
    }

    @Inject(method = "getRendertypeGlintDirectShader", at = @At("HEAD"), cancellable = true)
    private static void overrideGlintShader(CallbackInfoReturnable<ShaderInstance> cir) {
        EventGetGlintShaders eventGetGlintShaders = new EventGetGlintShaders(rendertypeGlintDirectShader).run();
        if (eventGetGlintShaders.isCancelled())
            cir.setReturnValue(eventGetGlintShaders.getShader());
    }

    @Inject(method = "getRendertypeArmorEntityGlintShader", at = @At("HEAD"), cancellable = true)
    private static void overrideGlintShader1(CallbackInfoReturnable<ShaderInstance> cir) {
        EventGetGlintShaders eventGetGlintShaders = new EventGetGlintShaders(rendertypeArmorEntityGlintShader).run();
        if (eventGetGlintShaders.isCancelled())
            cir.setReturnValue(eventGetGlintShaders.getShader());
    }

    @Inject(method = "getRendertypeArmorGlintShader", at = @At("HEAD"), cancellable = true)
    private static void overrideGlintShader2(CallbackInfoReturnable<ShaderInstance> cir) {
        EventGetGlintShaders eventGetGlintShaders = new EventGetGlintShaders(rendertypeArmorGlintShader).run();
        if (eventGetGlintShaders.isCancelled())
            cir.setReturnValue(eventGetGlintShaders.getShader());
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    public void renderHand(PoseStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        EventRenderHand eventRenderHand = new EventRenderHand().run();
        if (eventRenderHand.isCancelled())
            ci.cancel();
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void bobView1(PoseStack matrixStack, float f, CallbackInfo ci) {
        EventBobView eventBobView = new EventBobView().run();
        if (eventBobView.isCancelled())
            ci.cancel();
    }

    @Inject(method = "bobHurt", at = @At(value = "HEAD"), cancellable = true)
    public void bobViewWhenHurt1(PoseStack matrixStack, float float_1, CallbackInfo ci) {
        if (((EventHurtCam) new EventHurtCam().run()).isCancelled()) ci.cancel();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/LevelRenderer.doEntityOutline()V"))
    public void renderForEvent(float float_1, long long_1, boolean boolean_1, CallbackInfo ci) {
        new EventRender2DNoScale().run();
    }
}
