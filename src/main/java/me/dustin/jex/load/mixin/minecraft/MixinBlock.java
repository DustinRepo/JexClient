package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventShouldRenderFace;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void shouldDrawSide1(BlockState state, BlockGetter world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        try {
            EventShouldRenderFace eventShouldRenderFace = new EventShouldRenderFace(WorldHelper.INSTANCE.getBlock(pos), side, pos).run();
            if (eventShouldRenderFace.isCancelled())
                cir.setReturnValue(eventShouldRenderFace.isShouldDrawSide());
        } catch (Exception e) {
        }
    }

}
