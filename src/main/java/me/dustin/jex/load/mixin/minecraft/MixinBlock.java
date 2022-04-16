package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventShouldDrawSide;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void shouldDrawSide1(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        try {
            EventShouldDrawSide eventShouldDrawSide = new EventShouldDrawSide(WorldHelper.INSTANCE.getBlock(pos), side, pos).run();
            if (eventShouldDrawSide.isCancelled())
                cir.setReturnValue(eventShouldDrawSide.isShouldDrawSide());
        } catch (Exception e) {
        }
    }

}
