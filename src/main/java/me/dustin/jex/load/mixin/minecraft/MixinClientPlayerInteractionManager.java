package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventGetReachDistance;
import me.dustin.jex.event.player.EventHasExtendedReach;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.event.world.EventPlayerInteractionTick;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager {

    @Shadow private float currentBreakingProgress;

    @Shadow private int blockBreakingCooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick1(CallbackInfo ci) {
        new EventPlayerInteractionTick().run();
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    public void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        EventClickBlock eventClickBlock = new EventClickBlock(pos, direction).run();
        if (eventClickBlock.isCancelled())
            cir.setReturnValue(false);
    }

    @Inject(method = "breakBlock", at = @At("HEAD"))
    public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        new EventBreakBlock(WorldHelper.INSTANCE.getBlock(pos).getDefaultState(), pos).run();
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntity1(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventAttackEntity eventAttackEntity = new EventAttackEntity(target).run();
        if (eventAttackEntity.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> callback) {
        EventGetReachDistance eventGetReachDistance = new EventGetReachDistance().run();
        if (eventGetReachDistance.getReachDistance() != null)
            callback.setReturnValue(eventGetReachDistance.getReachDistance());
    }

    @Inject(method = "hasExtendedReach", at = @At("HEAD"), cancellable = true)
    private void hasExtendedReach(CallbackInfoReturnable<Boolean> callback) {
        EventHasExtendedReach eventHasExtendedReach = new EventHasExtendedReach().run();
        if (eventHasExtendedReach.isExtendedReach() != null)
            callback.setReturnValue(eventHasExtendedReach.isExtendedReach());
    }

    @Override
    public void setBlockBreakProgress(float progress) {
        this.currentBreakingProgress = progress;
    }


    @Override
    public void setBlockBreakingCooldown(int cooldown) {
        this.blockBreakingCooldown = cooldown;
    }

    @Override
    public float getBlockBreakProgress() {
        return this.currentBreakingProgress;
    }

    @Override
    public int getBlockBreakingCooldown() {
        return this.blockBreakingCooldown;
    }
}
