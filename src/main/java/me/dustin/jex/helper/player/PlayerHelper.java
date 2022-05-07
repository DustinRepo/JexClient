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
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public enum PlayerHelper {
    INSTANCE;

    private final ArrayList<UUID> requestedUUIDs = new ArrayList<>();
    private final ArrayList<String> requestedNames = new ArrayList<>();
    private final HashMap<UUID, String> nameMap = Maps.newHashMap();
    private final HashMap<String, UUID> uuidMap = Maps.newHashMap();
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
            if (Wrapper.INSTANCE.getLocalPlayer().getMainHandItem() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandItem().getItem() instanceof SwordItem) {
                Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND);
                Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.OFF_HAND);
            }
        } else {
            if (Wrapper.INSTANCE.getLocalPlayer().getOffhandItem() != null && Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() instanceof ShieldItem) {
                Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.OFF_HAND);
            }
        }
    }

    public void unblock() {
        NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
    }

    public float getYaw() {
        return Wrapper.INSTANCE.getLocalPlayer().getViewYRot(Wrapper.INSTANCE.getMinecraft().getFrameTime());
    }

    public float getPitch() {
        return Wrapper.INSTANCE.getLocalPlayer().getViewXRot(Wrapper.INSTANCE.getMinecraft().getFrameTime());
    }

    public void setYaw(float yaw) {
        Wrapper.INSTANCE.getLocalPlayer().setYRot(yaw);
    }

    public void setPitch(float pitch) {
        Wrapper.INSTANCE.getLocalPlayer().setXRot(pitch);
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
        Vec3 velo = Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement();
        Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(x, velo.y, velo.z);
    }

    public void setVelocityY(double y) {
        Vec3 velo = Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement();
        Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(velo.x, y, velo.z);
    }

    public void setVelocityZ(double z) {
        Vec3 velo = Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement();
        Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(velo.x, velo.y, z);
    }

    public void setVelocityX(Entity entity, double x) {
        Vec3 velo = entity.getDeltaMovement();
        entity.setDeltaMovement(x, velo.y, velo.z);
    }

    public void setVelocityY(Entity entity, double y) {
        Vec3 velo = entity.getDeltaMovement();
        entity.setDeltaMovement(velo.x, y, velo.z);
    }

    public void setVelocityZ(Entity entity, double z) {
        Vec3 velo = entity.getDeltaMovement();
        entity.setDeltaMovement(velo.x, velo.y, z);
    }

    public void placeBlockInPos(BlockPos blockPos, InteractionHand hand, boolean illegallPlace) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.below();
        BlockPos up = blockPos.above();

        BlockPos placePos = null;
        Direction placeDir = null;

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(north).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = north;
            placeDir = Direction.SOUTH;
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(south).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = south;
            placeDir = Direction.NORTH;
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(east).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = east;
            placeDir = Direction.WEST;
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(west).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = west;
            placeDir = Direction.EAST;
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(up).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = up;
            placeDir = Direction.DOWN;
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(down).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = down;
            placeDir = Direction.UP;
        }
        if (placePos == null) {
            if (illegallPlace) {
                Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), hand, new BlockHitResult(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.UP, blockPos, false));
                swing(hand);
            }
        } else {
            Vec3 placeVec = WorldHelper.INSTANCE.sideOfBlock(placePos, placeDir);
            BlockHitResult blockHitResult = new BlockHitResult(placeVec, placeDir, placePos, false);
            Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), hand, blockHitResult);
            swing(hand);
        }
    }

    public boolean canPlaceHere(BlockPos blockPos) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.below();
        BlockPos up = blockPos.above();

        BlockPos placePos = null;

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(north).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = north;
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(south).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = south;
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(east).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = east;
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(west).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = west;
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(up).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = up;
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable() && WorldHelper.INSTANCE.getBlockState(down).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS) {
            placePos = down;
        }
        return placePos != null;
    }

    public Vec3 getPlacingLookPos(BlockPos blockPos) {
        BlockPos north = blockPos.north();
        BlockPos east = blockPos.east();
        BlockPos south = blockPos.south();
        BlockPos west = blockPos.west();
        BlockPos down = blockPos.below();
        BlockPos up = blockPos.above();

        if (!WorldHelper.INSTANCE.getBlockState(north).getMaterial().isReplaceable()) {
            Direction direction = Direction.SOUTH;
            return ClientMathHelper.INSTANCE.getVec(north).add(0.5, 0.5, 0.5).add(direction.getStepX(), direction.getStepX(), direction.getStepX());
        } else if (!WorldHelper.INSTANCE.getBlockState(south).getMaterial().isReplaceable()) {
            Direction direction = Direction.NORTH;
            return ClientMathHelper.INSTANCE.getVec(south).add(0.5, 0.5, 0.5).add(direction.getStepX(), direction.getStepX(), direction.getStepX());
        } else if (!WorldHelper.INSTANCE.getBlockState(east).getMaterial().isReplaceable()) {
            Direction direction = Direction.WEST;
            return ClientMathHelper.INSTANCE.getVec(east).add(0.5, 0.5, 0.5).add(direction.getStepX(), direction.getStepX(), direction.getStepX());
        } else if (!WorldHelper.INSTANCE.getBlockState(west).getMaterial().isReplaceable()) {
            Direction direction = Direction.EAST;
            return ClientMathHelper.INSTANCE.getVec(west).add(0.5, 0.5, 0.5).add(direction.getStepX(), direction.getStepX(), direction.getStepX());
        } else if (!WorldHelper.INSTANCE.getBlockState(up).getMaterial().isReplaceable()) {
            Direction direction = Direction.DOWN;
            return ClientMathHelper.INSTANCE.getVec(up).add(0.5, 0.5, 0.5).add(direction.getStepX(), direction.getStepX(), direction.getStepX());
        } else if (!WorldHelper.INSTANCE.getBlockState(down).getMaterial().isReplaceable()) {
            Direction direction = Direction.UP;
            return ClientMathHelper.INSTANCE.getVec(down).add(0.5, 0.5, 0.5).add(direction.getStepX(), direction.getStepX(), direction.getStepX());
        }
        return ClientMathHelper.INSTANCE.getVec(blockPos);
    }

    public RotationVector rotateToEntity(Entity entityIn) {
        double xDif = entityIn.getX() - Wrapper.INSTANCE.getLocalPlayer().getX();
        double zDif = entityIn.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ();
        double yDif;

        if (entityIn instanceof LivingEntity livingEntity) {
            yDif = livingEntity.getY() + (double) livingEntity.getEyeHeight(livingEntity.getPose()) - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()));
        } else {
            yDif = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) (Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()) * Math.random()));
        }

        double var141 = Mth.sqrt((float)(xDif * xDif + zDif * zDif));
        float var12 = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(var12, var13);
    }

    public RotationVector rotateToCenter(Entity entityIn) {
        double xDif = entityIn.getX() - Wrapper.INSTANCE.getLocalPlayer().getX();
        double zDif = entityIn.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ();
        double yDif = (entityIn.getY() + entityIn.getBbHeight() / 2.f) - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()));

        double var141 = Mth.sqrt((float)(xDif * xDif + zDif * zDif));
        float var12 = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float var13 = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(var12, var13);
    }

    public RotationVector rotateToVec(Entity entityIn, Vec3 vec3d) {
        double xDif = vec3d.x - entityIn.getX();
        double zDif = vec3d.z - entityIn.getZ();
        double yDif = vec3d.y - (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D;

        double var141 = Mth.sqrt((float)(xDif * xDif + zDif * zDif));
        float yaw = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(yaw, pitch);
    }

    public RotationVector rotateFromVec(Vec3 vec3d, Entity entityIn) {
        double xDif = entityIn.getX() - vec3d.x;
        double zDif = entityIn.getZ() - vec3d.z;
        double yDif = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D - vec3d.y;

        double var141 = Mth.sqrt((float)(xDif * xDif + zDif * zDif));
        float yaw = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(yaw, pitch);
    }

    public RotationVector randomRotateTo(Entity ent2, float sideOffset, float heightOffset) {
        Random random = new Random();
        sideOffset = ent2.getBbWidth() * sideOffset;
        heightOffset = ent2.getBbHeight() * heightOffset;
        double xDif = (ent2.getX() - sideOffset + (random.nextFloat() * (sideOffset * 2))) - Wrapper.INSTANCE.getLocalPlayer().getX();
        double zDif = (ent2.getZ() - sideOffset + (random.nextFloat() * (sideOffset * 2))) - Wrapper.INSTANCE.getLocalPlayer().getZ();
        double yDif = (ent2.getY() + (double) (ent2.getBbHeight() / 2) - heightOffset + (random.nextFloat() * (heightOffset * 2))) - (Wrapper.INSTANCE.getLocalPlayer().getY() + (double) Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()));

        double var141 = Mth.sqrt((float)(xDif * xDif + zDif * zDif));
        float yaw = (float) (Math.atan2(zDif, xDif) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(yDif, var141) * 180.0D / Math.PI));
        return new RotationVector(yaw, pitch);
    }

    public Entity getCrosshairEntity(float tickDelta, RotationVector rots, float reach) {
        Entity entity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
        if (entity != null) {
            if (Wrapper.INSTANCE.getMinecraft().level != null) {
                Vec3 vec3d = entity.getEyePosition(tickDelta);
                Vec3 vec3d2 = getRotationVector(rots.getPitch(), rots.getYaw());
                Vec3 vec3d3 = vec3d.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);

                AABB box = entity.getBoundingBox().expandTowards(vec3d2.scale(reach)).inflate(1.0D, 1.0D, 1.0D);
                EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3d, vec3d3, box, (entityx) -> !entityx.isSpectator() && entityx.isPickable(), reach);
                if (entityHitResult != null) {
                    Entity entity2 = entityHitResult.getEntity();
                    if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrame) {
                        return entity2;
                    }
                }
            }
        }
        return null;
    }

    public Vec3 getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3((double) (i * j), (double) (-k), (double) (h * j));
    }

    public int getDistanceFromMouse(Entity entity) {
        RotationVector neededRotations = rotateToCenter(entity);
        RotationVector currentRotations = new RotationVector(getYaw(), getPitch());
        neededRotations.normalize();
        currentRotations.normalize();
        float neededYaw = currentRotations.getYaw() - neededRotations.getYaw();
        float neededPitch = currentRotations.getPitch() - neededRotations.getPitch();
        float distanceFromMouse = Mth.sqrt(neededYaw * neededYaw + neededPitch * neededPitch);
        return (int) distanceFromMouse;
    }

    public int getDistanceFromMouse(Vec3 vec3d) {
        RotationVector neededRotations = rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), vec3d);
        RotationVector currentRotations = new RotationVector(getYaw(), getPitch());
        neededRotations.normalize();
        currentRotations.normalize();
        float neededYaw = currentRotations.getYaw() - neededRotations.getYaw();
        float neededPitch = currentRotations.getPitch() - neededRotations.getPitch();
        float distanceFromMouse = Mth.sqrt(neededYaw * neededYaw + neededPitch * neededPitch);
        return (int) distanceFromMouse;
    }

    public double getWaterSpeed() {
        double speed = 1.96 / 20;
        int dsLevel = InventoryHelper.INSTANCE.getDepthStriderLevel();
        switch (dsLevel) {
            case 1 -> speed = 3.21 / 20;
            case 2 -> speed = 3.89 / 20;
            case 3 -> speed = 4.32 / 20;
        }
        return speed;
    }

    public double getWaterSpeed(int depthStriderLevel, boolean accountSprint) {
        double speed = switch (depthStriderLevel) {
            case 1 -> 3.21 / 20;
            case 2 -> 3.89 / 20;
            case 3 -> 4.32 / 20;
            default -> 1.96 / 20;
        };
        if ((Wrapper.INSTANCE.getLocalPlayer().isSprinting() || (Feature.getState(Sprint.class) && isMoving())) && accountSprint)
            speed += (speed * 0.3);
        return speed;
    }

    public boolean isMoving() {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            return false;
        return Wrapper.INSTANCE.getLocalPlayer().input.forwardImpulse != 0 || Wrapper.INSTANCE.getLocalPlayer().input.leftImpulse != 0;
    }

    public void setMoveSpeed(EventMove event, final double speed) {
        double forward = Wrapper.INSTANCE.getLocalPlayer().input.forwardImpulse;
        double strafe = Wrapper.INSTANCE.getLocalPlayer().input.leftImpulse;
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
        if (Wrapper.INSTANCE.getLocalPlayer().hasEffect(MobEffects.MOVEMENT_SPEED)) {
            final int amplifier = Wrapper.INSTANCE.getLocalPlayer().getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public void centerOnBlock() {
        double fracX = Mth.frac(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = Mth.frac(Wrapper.INSTANCE.getLocalPlayer().getZ());
        if (fracX < 0.3) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.3;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        } else if (fracX > 0.7) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.7;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        }

        if (fracZ < 0.3) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.3;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        } else if (fracZ > 0.7) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.7;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        }
    }

    public void centerPerfectlyOnBlock() {
        double fracX = Mth.frac(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = Mth.frac(Wrapper.INSTANCE.getLocalPlayer().getZ());
        if (fracX < 0.5) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        } else if (fracX > 0.5) {
            double x = Wrapper.INSTANCE.getLocalPlayer().getX() - fracX + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(x, Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), true));
        }

        if (fracZ < 0.5) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        } else if (fracZ > 0.5) {
            double z = Wrapper.INSTANCE.getLocalPlayer().getZ() - fracZ + 0.5;
            Wrapper.INSTANCE.getLocalPlayer().setPosRaw(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z);
            NetworkHelper.INSTANCE.sendPacket(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), z, true));
        }
    }

    public boolean isOnEdgeOfBlock() {
        double fracX = Mth.frac(Wrapper.INSTANCE.getLocalPlayer().getX());
        double fracZ = Mth.frac(Wrapper.INSTANCE.getLocalPlayer().getZ());
        return fracX < 0.3 || fracX > 0.7 || fracZ < 0.3 || fracZ > 0.7;
    }

    public Vec3 getPlayerVec() {
        return new Vec3(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
    }

    public void rightClickBlock(BlockPos blockPos, InteractionHand hand, boolean insideBlock) {
        BlockHitResult blockHitResult = new BlockHitResult(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.DOWN, blockPos, insideBlock);
        Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), hand, blockHitResult);
    }

    public ItemStack mainHandStack() {
        return Wrapper.INSTANCE.getLocalPlayer().getMainHandItem();
    }

    public ItemStack offHandStack() {
        return Wrapper.INSTANCE.getLocalPlayer().getOffhandItem();
    }

    public void useItem(InteractionHand hand) {
        Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getLocalPlayer(), hand);
    }

    public void stopUsingItem() {
        NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
    }

    public void swing(InteractionHand hand) {
        if (Wrapper.INSTANCE.getPlayer() == Freecam.playerEntity) {
            NetworkHelper.INSTANCE.sendPacket(new ServerboundSwingPacket(hand));
            Wrapper.INSTANCE.getPlayer().swing(hand);
        } else
            Wrapper.INSTANCE.getLocalPlayer().swing(hand);
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
        Wrapper.INSTANCE.getLocalPlayer().yHeadRot = yaw;
        Wrapper.INSTANCE.getLocalPlayer().yBodyRot = yaw;
    }, Priority.FIRST, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
