package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventShouldDrawSide;
import me.jellysquid.mods.sodium.client.render.occlusion.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(BlockOcclusionCache.class)
public class SodiumMixinBlockOcclusionCache {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true, remap = false)
    public void shouldDrawSide1(BlockState selfState, BlockView view, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> cir) {//lol errors because I neglected to create a -dev jar so it's all already mapped code in sodium but it still works so fuck it
        EventShouldDrawSide eventShouldDrawSide = new EventShouldDrawSide(selfState.getBlock(), pos).run();
        if (eventShouldDrawSide.isCancelled()) {
            cir.setReturnValue(eventShouldDrawSide.isShouldDrawSide());
        }
    }

}
