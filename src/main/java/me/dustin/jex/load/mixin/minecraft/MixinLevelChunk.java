package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class MixinLevelChunk {

    @Inject(method = "addAndRegisterBlockEntity", at = @At("HEAD"))
    public void addBlockEntity(BlockEntity blockEntity, CallbackInfo ci) {
        BlockPos blockPos = blockEntity.getBlockPos();
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
