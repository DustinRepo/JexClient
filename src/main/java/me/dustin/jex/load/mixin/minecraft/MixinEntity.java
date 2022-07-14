package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.event.player.EventPushAwayFromEntity;
import me.dustin.jex.event.player.EventSlowdown;
import me.dustin.jex.event.player.EventStep;
import me.dustin.jex.event.render.EventNametagShouldRender;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow public abstract Box getBoundingBox();

    @Shadow protected boolean onGround;

    @Shadow public abstract void move(MovementType type, Vec3d movement);

    @Shadow @Nullable public abstract AbstractTeam getScoreboardTeam();

    @Shadow private Box boundingBox;

    @Shadow public World world;

    @Shadow public float stepHeight;

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void push(Entity entity, CallbackInfo ci) {
        EventPushAwayFromEntity eventPushAwayFromEntity = new EventPushAwayFromEntity().run();
        if (eventPushAwayFromEntity.isCancelled())
            ci.cancel();
    }

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getBoundBox1(CallbackInfoReturnable<Box> cir) {
        EventEntityHitbox eventEntityHitbox = new EventEntityHitbox((Entity)(Object)this, this.boundingBox).run();
        cir.setReturnValue(eventEntityHitbox.getBox());
    }

    @Inject(method = "shouldRender(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void shouldRender(double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        EventNametagShouldRender eventNametagShouldRender = new EventNametagShouldRender((Entity) (Object) this).run();
        if (eventNametagShouldRender.isCancelled())
            ci.setReturnValue(true);
    }

    @Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
    public void slowMovement(BlockState blockState, Vec3d multiplier, CallbackInfo ci) {
        EventSlowdown eventSlowdown = null;
        if (blockState.getBlock() == Blocks.COBWEB)
            eventSlowdown = new EventSlowdown(EventSlowdown.State.COBWEB).run();
        if (blockState.getBlock() == Blocks.SWEET_BERRY_BUSH)
            eventSlowdown = new EventSlowdown(EventSlowdown.State.BERRY_BUSH).run();
        if (blockState.getBlock() == Blocks.POWDER_SNOW)
            eventSlowdown = new EventSlowdown(EventSlowdown.State.POWDERED_SNOW).run();

        if (eventSlowdown != null && eventSlowdown.isCancelled())
            ci.cancel();
    }

    @Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    public void move1(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        if (((Entity)(Object)this) != Wrapper.INSTANCE.getLocalPlayer() || Wrapper.INSTANCE.getLocalPlayer() == null)
            return;
        Box box = this.getBoundingBox();
        List<VoxelShape> list = this.world.getEntityCollisions((Entity)(Object)this, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == 0.0D ? movement : Entity.adjustMovementForCollisions((Entity)(Object)this, movement, box, this.world, list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.onGround || bl2 && movement.y < 0.0D;
        if (this.stepHeight > 0.0F && bl4 && (bl || bl3)) {
            new EventStep((Entity) (Object) this, EventStep.Mode.PRE, 0).run();
            Vec3d vec3d2 = Entity.adjustMovementForCollisions((Entity)(Object)this, new Vec3d(movement.x, (double) this.stepHeight, movement.z), box, this.world, list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions((Entity)(Object)this, new Vec3d(0.0D, (double) this.stepHeight, 0.0D), box.stretch(movement.x, 0.0D, movement.z), this.world, list);
            if (vec3d3.y < (double) this.stepHeight) {
                Vec3d vec3d4 = Entity.adjustMovementForCollisions((Entity)(Object)this, new Vec3d(movement.x, 0.0D, movement.z), box.offset(vec3d3), this.world, list).add(vec3d3);
                if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                    vec3d2 = vec3d4;
                }
            }

            Vec3d savedVec = vec3d2.add(Entity.adjustMovementForCollisions((Entity) (Object) this, new Vec3d(0.0D, -vec3d2.y + movement.y, 0.0D), box.offset(vec3d2), this.world, list));
            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                if (savedVec.y > 0.6f)
                    new EventStep((Entity) (Object) this, EventStep.Mode.MID, savedVec.y).run();
                cir.setReturnValue(savedVec);
                new EventStep((Entity) (Object) this, EventStep.Mode.END, savedVec.y).run();
                return;
            }
        }

        new EventStep((Entity)(Object)this, EventStep.Mode.POST, 0).run();
        cir.setReturnValue(vec3d);

    }

}
