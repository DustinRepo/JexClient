package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {

    @Inject(method = "addBlockEntity", at = @At("HEAD"))
    public void addBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        BlockPos blockPos = blockEntity.getPos();
        if (WorldHelper.INSTANCE.getBlockEntityList().containsKey(blockPos)) {
            WorldHelper.INSTANCE.getBlockEntityList().replace(blockPos, blockEntity);
        } else {
            WorldHelper.INSTANCE.getBlockEntityList().put(blockPos, blockEntity);
        }
    }

    @Inject(method = "removeBlockEntity", at = @At("HEAD"))
    public void removeBlockEntity(BlockPos blockPos, CallbackInfo ci) {
        WorldHelper.INSTANCE.getBlockEntityList().remove(blockPos);
    }
}
