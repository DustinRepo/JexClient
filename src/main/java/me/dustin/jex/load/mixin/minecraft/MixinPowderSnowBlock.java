package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventBlockCollisionShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
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

@Mixin(PowderSnowBlock.class)
public class MixinPowderSnowBlock extends Block {

    @Shadow @Final private static VoxelShape FALLING_SHAPE;

    public MixinPowderSnowBlock(Settings settings) {
        super(settings);
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> ci) {
        Entity entity;
        VoxelShape shape = null;
        if (context instanceof EntityShapeContext && (entity = ((EntityShapeContext)context).getEntity()) != null) {
            if (entity.fallDistance > 2.5f) {
                shape =  FALLING_SHAPE;
            }
            boolean bl = entity instanceof FallingBlockEntity;
            if (bl || PowderSnowBlock.canWalkOnPowderSnow(entity) && context.isAbove(VoxelShapes.fullCube(), pos, false) && !context.isDescending()) {
                shape = super.getCollisionShape(state, world, pos, context);
            }
        }

        EventBlockCollisionShape eventBlockCollisionShape = new EventBlockCollisionShape(pos, state.getBlock(), shape).run();
        if (eventBlockCollisionShape.isCancelled())
            ci.setReturnValue(eventBlockCollisionShape.getVoxelShape());
    }

}
