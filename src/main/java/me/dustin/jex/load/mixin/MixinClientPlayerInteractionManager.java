package me.dustin.jex.load.mixin;

import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

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
}
