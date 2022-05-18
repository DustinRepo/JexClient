package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventBlockCollisionShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CactusBlock.class)
public class MixinCactusBlock {

    @Shadow @Final protected static VoxelShape COLLISION_SHAPE;

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> ci) {
        EventBlockCollisionShape eventBlockCollisionShape = new EventBlockCollisionShape(pos, state.getBlock(), COLLISION_SHAPE).run();
        if (eventBlockCollisionShape.isCancelled())
            ci.setReturnValue(eventBlockCollisionShape.getVoxelShape());
    }

}
