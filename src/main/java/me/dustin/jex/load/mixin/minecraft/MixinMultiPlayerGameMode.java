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
import me.dustin.jex.load.impl.IMultiPlayerGameMode;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode implements IMultiPlayerGameMode {

    @Mutable
    @Shadow @Final private ClientPacketListener connection;

    @Shadow private float destroyProgress;

    @Shadow private int destroyDelay;

    @Shadow private BlockPos destroyBlockPos;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick1(CallbackInfo ci) {
        new EventPlayerInteractionTick().run();
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    public void attackBlockPre(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        EventClickBlock eventClickBlock = new EventClickBlock(pos, direction, EventClickBlock.Mode.PRE).run();
        if (eventClickBlock.isCancelled())
            cir.setReturnValue(false);
    }

    @Inject(method = "startDestroyBlock", at = @At("RETURN"), cancellable = true)
    public void attackBlockPost(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        new EventClickBlock(pos, direction, EventClickBlock.Mode.POST).run();
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void interactBlockPre(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        EventInteractBlock eventInteractBlock = new EventInteractBlock(hitResult.getBlockPos(), hitResult, EventInteractBlock.Mode.PRE).run();
        if (eventInteractBlock.isCancelled())
            cir.setReturnValue(InteractionResult.PASS);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"), cancellable = true)
    public void interactBlockPost(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        new EventInteractBlock(hitResult.getBlockPos(), hitResult, EventInteractBlock.Mode.POST).run();
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        new EventBreakBlock(WorldHelper.INSTANCE.getBlock(pos).defaultBlockState(), pos).run();
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void attackEntity1(Player player, Entity target, CallbackInfo ci) {
        EventAttackEntity eventAttackEntity = new EventAttackEntity(target).run();
        if (eventAttackEntity.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getPickRange", at = @At("HEAD"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> callback) {
        EventGetReachDistance eventGetReachDistance = new EventGetReachDistance().run();
        if (eventGetReachDistance.getReachDistance() != null)
            callback.setReturnValue(eventGetReachDistance.getReachDistance());
    }

    @Inject(method = "hasFarPickRange", at = @At("HEAD"), cancellable = true)
    private void hasExtendedReach(CallbackInfoReturnable<Boolean> callback) {
        EventHasExtendedReach eventHasExtendedReach = new EventHasExtendedReach().run();
        if (eventHasExtendedReach.isExtendedReach() != null)
            callback.setReturnValue(eventHasExtendedReach.isExtendedReach());
    }

    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    public void stopUsingItem(CallbackInfo ci) {
        EventStopUsingItem eventStopUsingItem = new EventStopUsingItem().run();
        if (eventStopUsingItem.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public void setBlockBreakProgress(float progress) {
        this.destroyProgress = progress;
    }


    @Override
    public void setBlockBreakingCooldown(int cooldown) {
        this.destroyDelay = cooldown;
    }

    @Override
    public float getBlockBreakProgress() {
        return this.destroyProgress;
    }

    @Override
    public int getBlockBreakingCooldown() {
        return this.destroyDelay;
    }

    @Override
    public BlockPos currentBreakingPos() {
        return this.destroyBlockPos;
    }

    @Override
    public void setClientPacketListener(ClientPacketListener clientPlayNetworkHandler) {
        this.connection = clientPlayNetworkHandler;
    }
}
