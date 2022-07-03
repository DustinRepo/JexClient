package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.event.render.*;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
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

    @Shadow protected abstract void bobView(MatrixStack matrices, float f);


    @Shadow @Final private Camera camera;

    @Shadow public abstract Matrix4f getBasicProjectionMatrix(double fov);

    @Shadow public abstract void loadProjectionMatrix(Matrix4f projectionMatrix);

    @Shadow protected abstract void bobViewWhenHurt(MatrixStack matrices, float tickDelta);

    @Shadow @Final private MinecraftClient client;

    @Shadow @Nullable private static Shader renderTypeTranslucentShader;

    @Shadow @Nullable private static Shader renderTypeGlintDirectShader;

    @Shadow @Nullable private static Shader renderTypeArmorEntityGlintShader;

    @Shadow @Nullable private static Shader renderTypeArmorGlintShader;

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "com/mojang/blaze3d/systems/RenderSystem.clear(IZ)V"))
    private void onRenderWorld(float partialTicks, long finishTimeNano, MatrixStack matrixStack1, CallbackInfo ci) {
        if (Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera == null || Wrapper.INSTANCE.getLocalPlayer() == null)
            return;
        RenderSystem.clearColor(1, 1, 1, 1);
        MatrixStack matrixStack = new MatrixStack();
        double d = this.getFov(camera, partialTicks, true);
        matrixStack.peek().getPositionMatrix().multiply(this.getBasicProjectionMatrix(d));
        loadProjectionMatrix(matrixStack.peek().getPositionMatrix());

        this.bobViewWhenHurt(matrixStack, partialTicks);
        Render3DHelper.INSTANCE.applyCameraRots(matrixStack);
        loadProjectionMatrix(matrixStack.peek().getPositionMatrix());
        new EventRender3D.EventRender3DNoBob(matrixStack, partialTicks).run();
        Render3DHelper.INSTANCE.fixCameraRots(matrixStack);
        if (this.client.options.getBobView().getValue()) {
            bobView(matrixStack, partialTicks);
        }
        loadProjectionMatrix(matrixStack.peek().getPositionMatrix());

        new EventRender3D(matrixStack1, partialTicks).run();
    }

    @Inject(method = "getRenderTypeTranslucentShader", at = @At("HEAD"), cancellable = true)
    private static void overrideTranslucentShader(CallbackInfoReturnable<Shader> cir) {
        EventGetTranslucentShader eventGetTranslucentShader = new EventGetTranslucentShader(renderTypeTranslucentShader).run();
        if (eventGetTranslucentShader.isCancelled())
            cir.setReturnValue(eventGetTranslucentShader.getShader());
    }

    @Inject(method = "getRenderTypeGlintDirectShader", at = @At("HEAD"), cancellable = true)
    private static void overrideGlintShader(CallbackInfoReturnable<Shader> cir) {
        EventGetGlintShaders eventGetGlintShaders = new EventGetGlintShaders(renderTypeGlintDirectShader).run();
        if (eventGetGlintShaders.isCancelled())
            cir.setReturnValue(eventGetGlintShaders.getShader());
    }

    @Inject(method = "getRenderTypeArmorEntityGlintShader", at = @At("HEAD"), cancellable = true)
    private static void overrideGlintShader1(CallbackInfoReturnable<Shader> cir) {
        EventGetGlintShaders eventGetGlintShaders = new EventGetGlintShaders(renderTypeArmorEntityGlintShader).run();
        if (eventGetGlintShaders.isCancelled())
            cir.setReturnValue(eventGetGlintShaders.getShader());
    }

    @Inject(method = "getRenderTypeArmorGlintShader", at = @At("HEAD"), cancellable = true)
    private static void overrideGlintShader2(CallbackInfoReturnable<Shader> cir) {
        EventGetGlintShaders eventGetGlintShaders = new EventGetGlintShaders(renderTypeArmorGlintShader).run();
        if (eventGetGlintShaders.isCancelled())
            cir.setReturnValue(eventGetGlintShaders.getShader());
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    public void renderHand(MatrixStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        EventRenderHand eventRenderHand = new EventRenderHand().run();
        if (eventRenderHand.isCancelled())
            ci.cancel();
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void bobView1(MatrixStack matrixStack, float f, CallbackInfo ci) {
        EventBobView eventBobView = new EventBobView().run();
        if (eventBobView.isCancelled())
            ci.cancel();
    }

    @Inject(method = "bobViewWhenHurt", at = @At(value = "HEAD"), cancellable = true)
    public void bobViewWhenHurt1(MatrixStack matrixStack, float float_1, CallbackInfo ci) {
        if (((EventHurtCam) new EventHurtCam().run()).isCancelled()) ci.cancel();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/render/WorldRenderer.drawEntityOutlinesFramebuffer()V"))
    public void renderForEvent(float float_1, long long_1, boolean boolean_1, CallbackInfo ci) {
        new EventRender2DNoScale().run();
    }
}
