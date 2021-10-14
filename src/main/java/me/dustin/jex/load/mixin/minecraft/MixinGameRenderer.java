package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.event.render.*;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.load.impl.IGameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceFactory;
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
public abstract class MixinGameRenderer implements IGameRenderer {

    @Shadow
    private int ticks;
    @Shadow
    @Final
    private Camera camera;
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract void loadProjectionMatrix(Matrix4f matrix4f);

    @Shadow
    protected abstract void bobView(MatrixStack matrixStack, float f);

    @Shadow
    protected abstract void bobViewWhenHurt(MatrixStack matrixStack, float f);

    @Shadow protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow public abstract Matrix4f getBasicProjectionMatrix(double d);

    @Shadow @Nullable private static Shader renderTypeGlintDirectShader;

    @Shadow @Nullable private static Shader renderTypeArmorEntityGlintShader;

    @Shadow @Nullable private static Shader renderTypeArmorGlintShader;

    @Shadow @Nullable private static Shader renderTypeTranslucentShader;

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "com/mojang/blaze3d/systems/RenderSystem.clear(IZ)V"))
    private void onRenderWorld(float partialTicks, long finishTimeNano, MatrixStack matrixStack1, CallbackInfo ci) {
        if (Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera == null)
            return;
        RenderSystem.clearColor(1, 1, 1, 1);
        MatrixStack matrixStack = new MatrixStack();
        double d = this.getFov(camera, partialTicks, true);
        matrixStack.peek().getModel().multiply(this.getBasicProjectionMatrix(d));
        loadProjectionMatrix(matrixStack.peek().getModel());
        //Sets up 3D render space for shaders
        Render3DHelper.INSTANCE.setup3DProj(partialTicks);

        this.bobViewWhenHurt(matrixStack, partialTicks);
        Render3DHelper.INSTANCE.applyCameraRots(matrixStack);
        loadProjectionMatrix(matrixStack.peek().getModel());
        new EventRender3D.EventRender3DNoBob(matrixStack, partialTicks).run();
        Render3DHelper.INSTANCE.fixCameraRots(matrixStack);
        if (this.client.options.bobView) {
            bobView(matrixStack, partialTicks);
        }
        loadProjectionMatrix(matrixStack.peek().getModel());

        new EventRender3D(matrixStack1, partialTicks).run();
    }

    @Inject(method = "preloadShaders", at = @At("RETURN"))
    public void preLoadShaders1(ResourceFactory factory, CallbackInfo ci) {
        ShaderHelper.loadCustomMCShaders(factory);
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

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "net/minecraft/util/profiler/Profiler.pop()V"))
    public void renderWorldBottom(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
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
        Render2DHelper.INSTANCE.setup2DProjection();
        new EventRender2DNoScale().run();
    }

    @Override
    public double getFOV(float tickDelta) {
        return this.getFov(camera, tickDelta, true);
    }
}
