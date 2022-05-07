package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventClipCamera;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Camera.class)
public class MixinCamera {

    @Inject(method = "getMaxZoom", at = @At(value = "INVOKE", target = "net/minecraft/world/phys/Vec3.distanceTo (Lnet/minecraft/world/phys/Vec3;)D"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void clipToSpace(double double_1, CallbackInfoReturnable<Double> cir, int int_1, float float_1, float float_2, float float_3, Vec3 vec3d_1, Vec3 vec3d_2, HitResult hitResult_1) {
        EventClipCamera eventClipCamera = new EventClipCamera().run();
        if (eventClipCamera.isCancelled())
            cir.setReturnValue(double_1);
    }

}
