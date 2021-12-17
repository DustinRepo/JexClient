package me.dustin.jex.helper.player;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Sprint;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public enum PlayerHelper {
    INSTANCE;

    private ArrayList<UUID> requestedUUIDs = new ArrayList<>();
    private ArrayList<String> requestedNames = new ArrayList<>();
    private HashMap<UUID, String> nameMap = Maps.newHashMap();
    private HashMap<String, UUID> uuidMap = Maps.newHashMap();
    private float yaw;
    private float pitch;

    public String getName(UUID uuid) {
        if (!requestedUUIDs.contains(uuid)) {
            new Thread(() -> {
                String name = MCAPIHelper.INSTANCE.getNameFromUUID(uuid);
                nameMap.put(uuid, name);
            }).start();
            requestedUUIDs.add(uuid);
        }
        return nameMap.get(uuid);
    }

    public UUID getUUID(String name) {
        if (!requestedNames.contains(name.toLowerCase())) {
            new Thread(() -> {
                UUID uuid = MCAPIHelper.INSTANCE.getUUIDFromName(name);
                uuidMap.put(name.toLowerCase(), uuid);
            }).start();
            requestedNames.add(name.toLowerCase());
        }
        return uuidMap.get(name.toLowerCase());
    }

    public void block(boolean ignoreNewCombat) {
        if (ignoreNewCombat) {
            if (Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() instanceof SwordItem) {
                Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND);
                Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.OFF_HAND);
            }
        } else {
            if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() instanceof ShieldItem) {
                Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.OFF_HAND);
            }
        }
    }

    public void unblock() {
        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }

    public float getYaw() {
        return Wrapper.INSTANCE.getLocalPlayer().getYaw(Wrapper.INSTANCE.getMinecraft().getTickDelta());
    }

    public float getPitch() {
        return Wrapper.INSTANCE.getLocalPlayer().getPitch(Wrapper.INSTANCE.getMinecraft().getTickDelta());
    }

    public void setYaw(float yaw) {
        Wrapper.INSTANCE.getLocalPlayer().setYaw(yaw);
    }

    public void setPitch(float pitch) {
        Wrapper.INSTANCE.getLocalPlayer().setPitch(pitch);
    }

    public void setRotation(RotationVector rotation) {
        setYaw(rotation.getYaw());
        setPitch(rotation.getPitch());
    }

    public void addYaw(float add) {
        setYaw(getYaw() + add);
    }

    public void addPitch(float add) {
        setPitch(getPitch() + add);
    }

    public void setVelocityX(double x) {
        Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
        Wrapper.INSTANCE.getLocalPlayer().setVelocity(x, velo.y, velo.z);
    }

    public void setVelocityY(double y) {
        Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
        Wrapper.INSTANCE.getLocalPlayer().setVelocity(velo.x, y, velo.z);
    }

    public void setVelocityZ(double z) {
        Vec3d velo = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
        Wrapper.INSTANCE.getLocalPlayer().setVelocity(velo.x, velo.y, z);
    }

    public void placeBlockInPos(BlockPos blockPos, Hand hand, boolean illegallPlace) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.down();
        BlockPos up = blockPos.up();

        BlockPos placePos = null;
        Direction placeDir = null;

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable()) {
            placePos = north;
            placeDir = Direction.SOUTH;
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable()) {
            placePos = south;
            placeDir = Direction.NORTH;
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable()) {
            placePos = east;
            placeDir = Direction.WEST;
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable()) {
            placePos = west;
            placeDir = Direction.EAST;
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable()) {
            placePos = up;
            placeDir = Direction.DOWN;
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable()) {
            placePos = down;
            placeDir = Direction.UP;
        }
        if (placePos == null || placeDir == null) {
            if (illegallPlace) {
                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), hand, new BlockHitResult(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.UP, blockPos, false));
                Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
            }
        } else {
            Vec3d placeVec = ClientMathHelper.INSTANCE.getVec(placePos);
            BlockHitResult blockHitResult = new BlockHitResult(placeVec, placeDir, placePos, false);
            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), hand, blockHitResult);
            Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
        }
    }

    public Vec3d getPlacingLookPos(BlockPos blockPos, boolean illegallPlace) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.down();
        BlockPos up = blockPos.up();

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable()) {
            Direction direction = Direction.SOUTH;
            return ClientMathHelper.INSTANCE.getVec(north).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable()) {
            Direction direction = Direction.NORTH;
            return ClientMathHelper.INSTANCE.getVec(south).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable()) {
            Direction direction = Direction.WEST;
            return ClientMathHelper.INSTANCE.getVec(east).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable()) {
            Direction direction = Direction.EAST;
            return ClientMathHelper.INSTANCE.getVec(west).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable()) {
            Direction direction = Direction.DOWN;
            return ClientMathHelper.INSTANCE.getVec(up).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable()) {
            Direction direction = Direction.UP;
            return ClientMathHelper.INSTANCE.getVec(down).add(0.5, 0.5, 0.5).add(direction.getOffsetX(), direction.getOffsetX(), direction.getOffsetX());
        }
        return ClientMathHelper.INSTANCE.getVec(blockPos);
    }

    public RotationVector getRotations(Entity entityIn, Entity ent2) {
        double var4 = entityIn.getX() - ent2.getX();
        double var8 = entityIn.getZ() - ent2.getZ();
        double var6;

        if (entityIn instanceof LivingEntity) {
            LivingEntity var14 = (LivingEntity) entityIn;
            var6 = var14.getY() + (double) var14.getEyeHeight(var14.getPose()) - (ent2.getY() + (double) ent2.getEyeHeight(ent2.getPose()));
        } else {
            var6 = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D - (ent2.getY() + (double) (ent2.getEyeHeight(ent2.getPose()) * Math.random()));
        }

        double var141 = (double) MathHelper.sqrt((float)(var4 * var4 + var8 * var8));
        float var12 = (float) (Math.atan2(var8, var4) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(var6, var141) * 180.0D / Math.PI));
        float pitch = updateRotation(EntityHelper.INSTANCE.getPitch(ent2), var13, Float.MAX_VALUE);
        float yaw = updateRotation(EntityHelper.INSTANCE.getYaw(ent2), var12, Float.MAX_VALUE);
        return new RotationVector(yaw - 180, -pitch);
    }

    public RotationVector getRotations(Entity entityIn, Vec3d vec3d) {
        double var4 = entityIn.getX() - vec3d.x;
        double var8 = entityIn.getZ() - vec3d.z;
        double var6;
        var6 = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D - vec3d.y;

        double var141 = (double) MathHelper.sqrt((float)(var4 * var4 + var8 * var8));
        float var12 = (float) (Math.atan2(var8, var4) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(var6, var141) * 180.0D / Math.PI));
        float pitch = updateRotation(getPitch(), var13, Float.MAX_VALUE);
        float yaw = updateRotation(getYaw(), var12, Float.MAX_VALUE);
        return new RotationVector(yaw - 180, -pitch);
    }

    public RotationVector getRotations(Vec3d vec3d, Entity entityIn) {
        double var4 = vec3d.x - entityIn.getX();
        double var8 = vec3d.z - entityIn.getZ();
        double var6;
        var6 = vec3d.y - (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D;

        double var141 = (double) MathHelper.sqrt((float)(var4 * var4 + var8 * var8));
        float var12 = (float) (Math.atan2(var8, var4) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(var6, var141) * 180.0D / Math.PI));
        float pitch = updateRotation(getPitch(), var13, Float.MAX_VALUE);
        float yaw = updateRotation(getYaw(), var12, Float.MAX_VALUE);
        return new RotationVector(yaw - 180, -pitch);
    }

    public RotationVector getRotations(Entity ent2, float sideOffset, float heightOffset) {
        ClientPlayerEntity entityIn = Wrapper.INSTANCE.getLocalPlayer();
        Random random = new Random();
        sideOffset = ent2.getWidth() * sideOffset;
        heightOffset = ent2.getHeight() * heightOffset;
        double var4 = entityIn.getX() - (ent2.getX() - sideOffset + (random.nextFloat() * (sideOffset * 2)));
        double var8 = entityIn.getZ() - (ent2.getZ() - sideOffset + (random.nextFloat() * (sideOffset * 2)));
        double var6;

        var6 = entityIn.getY() + (double) entityIn.getEyeHeight(entityIn.getPose()) - (ent2.getY() + (double) (ent2.getHeight() / 2) - heightOffset + (random.nextFloat() * (heightOffset * 2)));

        double var141 = MathHelper.sqrt((float)(var4 * var4 + var8 * var8));
        float var12 = (float) (Math.atan2(var8, var4) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(var6, var141) * 180.0D / Math.PI));
        float pitch = updateRotation(EntityHelper.INSTANCE.getPitch(ent2), var13, Float.MAX_VALUE);
        float yaw = updateRotation(EntityHelper.INSTANCE.getYaw(ent2), var12, Float.MAX_VALUE);
        return new RotationVector(yaw - 180, -pitch);
    }

    /**
     * Arguments: current rotation, intended rotation, max increment.
     */
    public float updateRotation(float angle, float targetAngle, float maxIncrease) {
        float f = MathHelper.wrapDegrees(targetAngle - angle);

        if (f > maxIncrease) {
            f = maxIncrease;
        }

        if (f < -maxIncrease) {
            f = -maxIncrease;
        }

        return angle + f;
    }

    public Entity getCrosshairEntity(float tickDelta, RotationVector rots, float reach) {
        Entity entity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
        if (entity != null) {
            if (Wrapper.INSTANCE.getMinecraft().world != null) {
                Vec3d vec3d = entity.getCameraPosVec(tickDelta);
                Vec3d vec3d2 = getRotationVector(rots.getPitch(), rots.getYaw());
                Vec3d vec3d3 = vec3d.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);

                Box box = entity.getBoundingBox().stretch(vec3d2.multiply(reach)).expand(1.0D, 1.0D, 1.0D);
                EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, (entityx) -> {
                    return !entityx.isSpectator() && entityx.collides();
                }, reach);
                if (entityHitResult != null) {
                    Entity entity2 = entityHitResult.getEntity();
                    if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrameEntity) {
                        return entity2;
                    }
                }
            }
        }
        return null;
    }

    public Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((double) (i * j), (double) (-k), (double) (h * j));
    }

    public double getWaterSpeed() {
        double speed = 1.96 / 20;
        int dsLevel = InventoryHelper.INSTANCE.getDepthStriderLevel();
        switch (dsLevel) {
            case 1:
                speed = 3.21 / 20;
                break;
            case 2:
                speed = 3.89 / 20;
                break;
            case 3:
                speed = 4.32 / 20;
                break;
        }
        return speed;
    }

    public double getWaterSpeed(int depthStriderLevel, boolean accountSprint) {
        double speed = 1.96 / 20;
        switch (depthStriderLevel) {
            case 1:
                speed = 3.21 / 20;
                break;
            case 2:
                speed = 3.89 / 20;
                break;
            case 3:
                speed = 4.32 / 20;
                break;
        }
        if ((Wrapper.INSTANCE.getLocalPlayer().isSprinting() || (Feature.getState(Sprint.class) && isMoving())) && accountSprint)
            speed += (speed * 0.3);
        return speed;
    }

    public boolean isMoving() {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            return false;
        return Wrapper.INSTANCE.getLocalPlayer().input.movementForward != 0 || Wrapper.INSTANCE.getLocalPlayer().input.movementSideways != 0;
    }

    public void setMoveSpeed(EventMove event, final double speed) {
        double forward = Wrapper.INSTANCE.getLocalPlayer().input.movementForward;
        double strafe = Wrapper.INSTANCE.getLocalPlayer().input.movementSideways;
        float yaw = this.yaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            event.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)));
            event.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
        }
    }

    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.SPEED)) {
            final int amplifier = Wrapper.INSTANCE.getLocalPlayer().getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public void centerOnBlock() {
        double fracX = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getZ());
        if (fracX < 0.3) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.3;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        } else if (fracX > 0.7) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.7;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        }

        if (fracZ < 0.3) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.3;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        } else if (fracZ > 0.7) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.7;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        }
    }

    public void centerPerfectlyOnBlock() {
        double fracX = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getZ());
        if (fracX < 0.5) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        } else if (fracX > 0.5) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        }

        if (fracZ < 0.5) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        } else if (fracZ > 0.5) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        }
    }

    public boolean isOnEdgeOfBlock() {
        double fracX = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = MathHelper.fractionalPart(Wrapper.INSTANCE.getLocalPlayer().getZ());
        return fracX < 0.3 || fracX > 0.7 || fracZ < 0.3 || fracZ > 0.7;
    }

    public Vec3d getPlayerVec() {
        return new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
    }

    public void rightClickBlock(BlockPos blockPos, Hand hand, boolean insideBlock) {
        BlockHitResult blockHitResult = new BlockHitResult(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.DOWN, blockPos, insideBlock);
        Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), hand, blockHitResult);
    }

    public ItemStack mainHandStack() {
        return Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
    }

    public ItemStack offHandStack() {
        return Wrapper.INSTANCE.getLocalPlayer().getOffHandStack();
    }

    public void useItem(Hand hand) {
        Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), hand);
    }

    public void stopUsingItem() {
        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }

    public void swing(Hand hand) {
        Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
    }

    public float getYawWithBaritone() {
        return this.yaw;
    }

    public float getPitchWithBaritone() {
        return this.pitch;
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.yaw = event.getYaw();
        this.pitch = event.getPitch();

        if (!BaritoneHelper.INSTANCE.baritoneExists())
            return;
        if (!BaritoneHelper.INSTANCE.isBaritoneRunning())
            return;
        Wrapper.INSTANCE.getLocalPlayer().headYaw = yaw;
        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = yaw;
    }, Priority.FIRST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
