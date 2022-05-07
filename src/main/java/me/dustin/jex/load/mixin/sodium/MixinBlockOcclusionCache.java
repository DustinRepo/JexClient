package me.dustin.jex.load.mixin.sodium;

import me.dustin.jex.event.render.EventShouldRenderFace;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(BlockOcclusionCache.class)
public class MixinBlockOcclusionCache {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true, remap = false)
    public void shouldDrawSide1(BlockState selfState, BlockGetter view, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        EventShouldRenderFace eventShouldRenderFace = new EventShouldRenderFace(selfState.getBlock(), facing, pos).run();
        if (eventShouldRenderFace.isCancelled()) {
            cir.setReturnValue(eventShouldRenderFace.isShouldDrawSide());
        }
    }

}
