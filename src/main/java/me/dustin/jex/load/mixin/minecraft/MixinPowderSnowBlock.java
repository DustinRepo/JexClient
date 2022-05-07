package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventBlockCollisionShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class MixinPowderSnowBlock extends Block {

    @Shadow @Final private static VoxelShape FALLING_COLLISION_SHAPE;

    public MixinPowderSnowBlock(Properties settings) {
        super(settings);
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    public void getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> ci) {
        Entity entity;
        VoxelShape shape = null;
        if (context instanceof EntityCollisionContext && (entity = ((EntityCollisionContext)context).getEntity()) != null) {
            if (entity.fallDistance > 2.5f) {
                shape =  FALLING_COLLISION_SHAPE;
            }
            boolean bl = entity instanceof FallingBlockEntity;
            if (bl || PowderSnowBlock.canEntityWalkOnPowderSnow(entity) && context.isAbove(Shapes.block(), pos, false) && !context.isDescending()) {
                shape = super.getCollisionShape(state, world, pos, context);
            }
        }

        EventBlockCollisionShape eventBlockCollisionShape = new EventBlockCollisionShape(pos, state.getBlock(), shape).run();
        if (eventBlockCollisionShape.isCancelled())
            ci.setReturnValue(eventBlockCollisionShape.getVoxelShape());
    }

}
