package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventWaterFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidState.class)
public class MixinFluidState {

    @Inject(method = "getFlow", at = @At("HEAD"), cancellable = true)
    public void getVelocity(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        if (((EventWaterFlow) new EventWaterFlow().run()).isCancelled())
            cir.setReturnValue(Vec3.ZERO);
    }

}
