package me.dustin.jex.load.mixin;

import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.event.player.EventPushAwayFromEntity;
import me.dustin.jex.event.player.EventSlowdown;
import me.dustin.jex.event.player.EventStep;
import me.dustin.jex.event.render.EventNametagShouldRender;
import me.dustin.jex.event.render.EventOutlineColor;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.movement.CompatSwim;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.collection.ReusableStream;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    public abstract AbstractTeam getScoreboardTeam();

    @Shadow
    protected abstract void setFlag(int index, boolean value);

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract boolean isAlive();

    @Shadow public abstract Box getBoundingBox();

    @Shadow public World world;

    @Shadow
    public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, ShapeContext context, ReusableStream<VoxelShape> collisions) {
        boolean bl = movement.x == 0.0D;
        boolean bl2 = movement.y == 0.0D;
        boolean bl3 = movement.z == 0.0D;
        if ((!bl || !bl2) && (!bl || !bl3) && (!bl2 || !bl3)) {
            ReusableStream<VoxelShape> reusableStream = new ReusableStream(Stream.concat(collisions.stream(), world.getBlockCollisions(entity, entityBoundingBox.stretch(movement))));
            return adjustMovementForCollisions(movement, entityBoundingBox, reusableStream);
        } else {
            return adjustSingleAxisMovementForCollisions(movement, entityBoundingBox, world, context, collisions);
        }
    }

    @Shadow protected boolean onGround;

    @Shadow public float stepHeight;

    @Shadow
    public static double squaredHorizontalLength(Vec3d vector)  {
        return vector.x * vector.x + vector.z * vector.z;
    }

    @Shadow
    public static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, ReusableStream<VoxelShape> collisions) {
        double d = movement.x;
        double e = movement.y;
        double f = movement.z;
        if (e != 0.0D) {
            e = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, collisions.stream(), e);
            if (e != 0.0D) {
                entityBoundingBox = entityBoundingBox.offset(0.0D, e, 0.0D);
            }
        }

        boolean bl = Math.abs(d) < Math.abs(f);
        if (bl && f != 0.0D) {
            f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions.stream(), f);
            if (f != 0.0D) {
                entityBoundingBox = entityBoundingBox.offset(0.0D, 0.0D, f);
            }
        }

        if (d != 0.0D) {
            d = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, collisions.stream(), d);
            if (!bl && d != 0.0D) {
                entityBoundingBox = entityBoundingBox.offset(d, 0.0D, 0.0D);
            }
        }

        if (!bl && f != 0.0D) {
            f = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, collisions.stream(), f);
        }

        return new Vec3d(d, e, f);
    }

    @Shadow
    public static Vec3d adjustSingleAxisMovementForCollisions(Vec3d movement, Box entityBoundingBox, WorldView world, ShapeContext context, ReusableStream<VoxelShape> collisions) {
        double d = movement.x;
        double e = movement.y;
        double f = movement.z;
        if (e != 0.0D) {
            e = VoxelShapes.calculatePushVelocity(Direction.Axis.Y, entityBoundingBox, world, e, context, collisions.stream());
            if (e != 0.0D) {
                entityBoundingBox = entityBoundingBox.offset(0.0D, e, 0.0D);
            }
        }

        boolean bl = Math.abs(d) < Math.abs(f);
        if (bl && f != 0.0D) {
            f = VoxelShapes.calculatePushVelocity(Direction.Axis.Z, entityBoundingBox, world, f, context, collisions.stream());
            if (f != 0.0D) {
                entityBoundingBox = entityBoundingBox.offset(0.0D, 0.0D, f);
            }
        }

        if (d != 0.0D) {
            d = VoxelShapes.calculatePushVelocity(Direction.Axis.X, entityBoundingBox, world, d, context, collisions.stream());
            if (!bl && d != 0.0D) {
                entityBoundingBox = entityBoundingBox.offset(d, 0.0D, 0.0D);
            }
        }

        if (!bl && f != 0.0D) {
            f = VoxelShapes.calculatePushVelocity(Direction.Axis.Z, entityBoundingBox, world, f, context, collisions.stream());
        }

        return new Vec3d(d, e, f);
    }

    @Shadow public abstract void move(MovementType type, Vec3d movement);

    @Shadow private Box entityBounds;

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void push(Entity entity, CallbackInfo ci) {
        EventPushAwayFromEntity eventPushAwayFromEntity = new EventPushAwayFromEntity().run();
        if (eventPushAwayFromEntity.isCancelled())
            ci.cancel();
    }

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getBoundBox1(CallbackInfoReturnable<Box> cir) {
        EventEntityHitbox eventEntityHitbox = new EventEntityHitbox((Entity)(Object)this, this.entityBounds).run();
        cir.setReturnValue(eventEntityHitbox.getBox());
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    public void getTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        AbstractTeam abstractTeam = this.getScoreboardTeam();
        int o = abstractTeam != null && abstractTeam.getColor().getColorValue() != null ? abstractTeam.getColor().getColorValue() : 16777215;
        EventOutlineColor eventOutlineColor = new EventOutlineColor(o, (Entity) (Object) this).run();
        cir.setReturnValue(eventOutlineColor.getColor());
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

        if (eventSlowdown != null && eventSlowdown.isCancelled())
            ci.cancel();
    }

    @Inject(method = "isInSwimmingPose", at = @At("HEAD"), cancellable = true)
    public void isInSwimmingPose(CallbackInfoReturnable<Boolean> ci) {
        Entity me = (Entity) (Object) this;
        if (Module.get(CompatSwim.class).getState()) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "getPose", at = @At("HEAD"), cancellable = true)
    public void getPose(CallbackInfoReturnable<EntityPose> ci) {
        if (Module.get(CompatSwim.class).getState()) {
            if (this.isSwimming()) {
                ci.setReturnValue(this.isAlive() ? (this.isSneaking() ? EntityPose.CROUCHING : EntityPose.STANDING) : EntityPose.DYING);
            }
        }
    }

    @Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    public void move1(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        Box box = this.getBoundingBox();
        ShapeContext shapeContext = ShapeContext.of((Entity)(Object)this);
        VoxelShape voxelShape = this.world.getWorldBorder().asVoxelShape();
        Stream<VoxelShape> stream = VoxelShapes.matchesAnywhere(voxelShape, VoxelShapes.cuboid(box.contract(1.0E-7D)), BooleanBiFunction.AND) ? Stream.empty() : Stream.of(voxelShape);
        Stream<VoxelShape> stream2 = this.world.getEntityCollisions((Entity)(Object)this, box.stretch(movement), (entity) -> {
            return true;
        });
        ReusableStream<VoxelShape> reusableStream = new ReusableStream(Stream.concat(stream2, stream));
        Vec3d vec3d = movement.lengthSquared() == 0.0D ? movement : adjustMovementForCollisions((Entity)(Object)this, movement, box, this.world, shapeContext, reusableStream);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.onGround || bl2 && movement.y < 0.0D;
        if (this.stepHeight > 0.0F && bl4 && (bl || bl3)) {
            new EventStep((Entity)(Object)this, EventStep.Mode.PRE, 0).run();
            Vec3d vec3d2 = adjustMovementForCollisions((Entity)(Object)this, new Vec3d(movement.x, (double)this.stepHeight, movement.z), box, this.world, shapeContext, reusableStream);
            Vec3d vec3d3 = adjustMovementForCollisions((Entity)(Object)this, new Vec3d(0.0D, (double)this.stepHeight, 0.0D), box.stretch(movement.x, 0.0D, movement.z), this.world, shapeContext, reusableStream);
            if (vec3d3.y < (double)this.stepHeight) {
                Vec3d vec3d4 = adjustMovementForCollisions((Entity)(Object)this, new Vec3d(movement.x, 0.0D, movement.z), box.offset(vec3d3), this.world, shapeContext, reusableStream).add(vec3d3);
                if (squaredHorizontalLength(vec3d4) > squaredHorizontalLength(vec3d2)) {
                    vec3d2 = vec3d4;
                }
            }
            Vec3d savedVec = vec3d2.add(adjustMovementForCollisions((Entity)(Object)this, new Vec3d(0.0D, -vec3d2.y + movement.y, 0.0D), box.offset(vec3d2), this.world, shapeContext, reusableStream));
            if (squaredHorizontalLength(vec3d2) > squaredHorizontalLength(vec3d)) {
                if (savedVec.y > 0.6f)
                    new EventStep((Entity)(Object)this, EventStep.Mode.MID, savedVec.y).run();
                cir.setReturnValue(savedVec);
                new EventStep((Entity)(Object)this, EventStep.Mode.END, savedVec.y).run();
                return;
            }
        }

        new EventStep((Entity)(Object)this, EventStep.Mode.POST, 0).run();
        cir.setReturnValue(vec3d);

    }

}
