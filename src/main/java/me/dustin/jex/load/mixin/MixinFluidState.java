package me.dustin.jex.load.mixin;

import me.dustin.jex.event.world.EventWaterVelocity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidState.class)
public class MixinFluidState {

    @Inject(method = "getVelocity", at = @At("HEAD"), cancellable = true)
    public void getVelocity(BlockView world, BlockPos pos, CallbackInfoReturnable<Vec3d> cir) {
        if (((EventWaterVelocity) new EventWaterVelocity().run()).isCancelled())
            cir.setReturnValue(Vec3d.ZERO);
    }

}
