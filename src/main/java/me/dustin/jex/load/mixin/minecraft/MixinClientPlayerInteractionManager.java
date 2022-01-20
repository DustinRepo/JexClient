package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventGetReachDistance;
import me.dustin.jex.event.player.EventHasExtendedReach;
import me.dustin.jex.event.player.EventStopUsingItem;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.event.world.EventInteractBlock;
import me.dustin.jex.event.world.EventPlayerInteractionTick;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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

    @Shadow private BlockPos currentBreakingPos;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick1(CallbackInfo ci) {
        new EventPlayerInteractionTick().run();
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    public void attackBlockPre(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        EventClickBlock eventClickBlock = new EventClickBlock(pos, direction, EventClickBlock.Mode.PRE).run();
        if (eventClickBlock.isCancelled())
            cir.setReturnValue(false);
    }

    @Inject(method = "attackBlock", at = @At("RETURN"), cancellable = true)
    public void attackBlockPost(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        new EventClickBlock(pos, direction, EventClickBlock.Mode.POST).run();
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    public void interactBlockPre(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        EventInteractBlock eventInteractBlock = new EventInteractBlock(hitResult.getBlockPos(), hitResult, EventInteractBlock.Mode.PRE).run();
        if (eventInteractBlock.isCancelled())
            cir.setReturnValue(ActionResult.PASS);
    }

    @Inject(method = "interactBlock", at = @At("RETURN"), cancellable = true)
    public void interactBlockPost(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        new EventInteractBlock(hitResult.getBlockPos(), hitResult, EventInteractBlock.Mode.POST).run();
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

    @Inject(method = "stopUsingItem", at = @At("HEAD"), cancellable = true)
    public void stopUsingItem(CallbackInfo ci) {
        EventStopUsingItem eventStopUsingItem = new EventStopUsingItem().run();
        if (eventStopUsingItem.isCancelled()) {
            ci.cancel();
        }
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

    @Override
    public BlockPos currentBreakingPos() {
        return this.currentBreakingPos;
    }
}
