package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventBlockCollisionShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class MixinFluidBlock {

    @Shadow
    @Final
    public static VoxelShape COLLISION_SHAPE;

    @Shadow
    @Final
    public static IntProperty LEVEL;

    @Shadow
    @Final
    protected FlowableFluid fluid;

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> ci) {
        EventBlockCollisionShape eventBlockCollisionShape = new EventBlockCollisionShape(pos, state.getBlock(), context.isAbove(this.COLLISION_SHAPE, pos, true) && (Integer)state.get(LEVEL) == 0 && context.canWalkOnFluid(world.getFluidState(pos.up()), state.getFluidState()) ? COLLISION_SHAPE : VoxelShapes.empty()).run();
        if (eventBlockCollisionShape.isCancelled())
            ci.setReturnValue(eventBlockCollisionShape.getVoxelShape());
    }

}
