package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.event.player.EventPushAwayFromEntity;
import me.dustin.jex.event.player.EventSlowdown;
import me.dustin.jex.event.player.EventStep;
import me.dustin.jex.event.render.EventNametagShouldRender;
import me.dustin.jex.event.render.EventTeamColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Team;
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

    @Shadow public abstract AABB getBoundingBox();

    @Shadow protected boolean onGround;

    @Shadow public abstract void move(MoverType type, Vec3 movement);

    @Shadow @Nullable public abstract Team getTeam();

    @Shadow private AABB bb;

    @Shadow public Level level;

    @Shadow public float maxUpStep;

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    public void push(Entity entity, CallbackInfo ci) {
        EventPushAwayFromEntity eventPushAwayFromEntity = new EventPushAwayFromEntity().run();
        if (eventPushAwayFromEntity.isCancelled())
            ci.cancel();
    }

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getBoundBox1(CallbackInfoReturnable<AABB> cir) {
        EventEntityHitbox eventEntityHitbox = new EventEntityHitbox((Entity)(Object)this, this.bb).run();
        cir.setReturnValue(eventEntityHitbox.getBox());
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        Team abstractTeam = this.getTeam();
        int o = abstractTeam != null && abstractTeam.getColor().getColor() != null ? abstractTeam.getColor().getColor() : 16777215;
        EventTeamColor eventTeamColor = new EventTeamColor(o, (Entity) (Object) this).run();
        cir.setReturnValue(eventTeamColor.getColor());
    }

    @Inject(method = "shouldRender(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void shouldRender(double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        EventNametagShouldRender eventNametagShouldRender = new EventNametagShouldRender((Entity) (Object) this).run();
        if (eventNametagShouldRender.isCancelled())
            ci.setReturnValue(true);
    }

    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    public void slowMovement(BlockState blockState, Vec3 multiplier, CallbackInfo ci) {
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

    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    public void move1(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        AABB box = this.getBoundingBox();
        List<VoxelShape> list = this.level.getEntityCollisions((Entity)(Object)this, box.expandTowards(movement));
        Vec3 vec3d = movement.lengthSqr() == 0.0D ? movement : Entity.collideBoundingBox((Entity)(Object)this, movement, box, this.level, list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.onGround || bl2 && movement.y < 0.0D;
        if (this.maxUpStep > 0.0F && bl4 && (bl || bl3)) {
            new EventStep((Entity) (Object) this, EventStep.Mode.PRE, 0).run();
            Vec3 vec3d2 = Entity.collideBoundingBox((Entity)(Object)this, new Vec3(movement.x, (double) this.maxUpStep, movement.z), box, this.level, list);
            Vec3 vec3d3 = Entity.collideBoundingBox((Entity)(Object)this, new Vec3(0.0D, (double) this.maxUpStep, 0.0D), box.expandTowards(movement.x, 0.0D, movement.z), this.level, list);
            if (vec3d3.y < (double) this.maxUpStep) {
                Vec3 vec3d4 = Entity.collideBoundingBox((Entity)(Object)this, new Vec3(movement.x, 0.0D, movement.z), box.move(vec3d3), this.level, list).add(vec3d3);
                if (vec3d4.horizontalDistanceSqr() > vec3d2.horizontalDistanceSqr()) {
                    vec3d2 = vec3d4;
                }
            }

            Vec3 savedVec = vec3d2.add(Entity.collideBoundingBox((Entity) (Object) this, new Vec3(0.0D, -vec3d2.y + movement.y, 0.0D), box.move(vec3d2), this.level, list));
            if (vec3d2.horizontalDistanceSqr() > vec3d.horizontalDistanceSqr()) {
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
