package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventClipCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Camera.class)
public class MixinCamera {

    @Inject(method = "clipToSpace", at = @At(value = "HEAD", target = "net/minecraft/util/math/Vec3d.distanceTo(Lnet/minecraft/util/math/Vec3d;)D"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void clipToSpace(double double_1, CallbackInfoReturnable<Double> cir, int int_1, float float_1, float float_2, float float_3, Vec3d vec3d_1, Vec3d vec3d_2, HitResult hitResult_1) {
        EventClipCamera eventClipCamera = new EventClipCamera().run();
        if (eventClipCamera.isCancelled())
            cir.setReturnValue(double_1);
    }

}
