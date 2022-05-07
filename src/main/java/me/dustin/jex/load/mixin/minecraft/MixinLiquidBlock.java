package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventBlockCollisionShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
public class MixinLiquidBlock {

    @Shadow
    @Final
    public static IntegerProperty LEVEL;

    @Shadow
    @Final
    protected FlowingFluid fluid;

    @Shadow @Final public static VoxelShape STABLE_SHAPE;

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> ci) {
        EventBlockCollisionShape eventBlockCollisionShape = new EventBlockCollisionShape(pos, state.getBlock(), context.isAbove(STABLE_SHAPE, pos, true) && (Integer)state.getValue(LEVEL) == 0 && context.canStandOnFluid(world.getFluidState(pos.above()), state.getFluidState()) ? STABLE_SHAPE : Shapes.empty()).run();
        if (eventBlockCollisionShape.isCancelled())
            ci.setReturnValue(eventBlockCollisionShape.getVoxelShape());
    }

}
