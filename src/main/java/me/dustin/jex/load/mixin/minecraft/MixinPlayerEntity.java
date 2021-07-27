package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.player.EventAttackCooldownPerTick;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.load.impl.IPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements IPlayerEntity {

    @Shadow
    @Final
    private PlayerAbilities abilities;

    @Override
    public PlayerAbilities getPlayerAbilities() {
        return this.abilities;
    }

    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("HEAD"), cancellable = true)
    public void getAttackCooldownProgress(CallbackInfoReturnable<Float> ci) {
        EventAttackCooldownPerTick eventAttackCooldownPerTick = new EventAttackCooldownPerTick().run();
        if (eventAttackCooldownPerTick.getValue() != -1)
            ci.setReturnValue((float) (eventAttackCooldownPerTick.getValue() / ((PlayerEntity) (Object) this).getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED).getValue() * 20.0D));
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    public void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        EventWalkOffBlock eventWalkOffBlock = new EventWalkOffBlock().run();
        cir.setReturnValue(((PlayerEntity) (Object) this).isSneaking() || eventWalkOffBlock.isCancelled());
    }
}
