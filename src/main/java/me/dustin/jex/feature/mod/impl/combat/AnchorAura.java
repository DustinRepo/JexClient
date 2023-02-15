package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import me.dustin.events.core.annotate.EventPointer;
import java.awt.*;

public class AnchorAura extends Feature {

    public final Property<TargetMode> modeProperty = new Property.PropertyBuilder<TargetMode>(this.getClass())
            .name("Mode")
            .value(TargetMode.SUICIDAL)
            .build();
    public final Property<AttackMode> attackModeProperty = new Property.PropertyBuilder<AttackMode>(this.getClass())
            .name("Explode")
            .description("The targeting mode for anchors.")
            .value(AttackMode.ANY)
            .build();
    public final Property<Long> attackDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Attack Delay (MS)")
            .value(200L)
            .min(0)
            .max(2000)
            .inc(10)
            .build();
    public final Property<Boolean> autoPlaceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Auto Place")
            .value(false)
            .build();
    public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();
    public final Property<Boolean> visualizeProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Visualize")
            .value(true)
            .parent(autoPlaceProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> onlyShowPlacementsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Only show placements")
            .value(false)
            .parent(visualizeProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Color> thinkingColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Thinking Color")
            .value(new Color(0, 150, 255))
            .parent(visualizeProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Color> placingColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Placing Color")
            .value(new Color(255, 0, 0))
            .parent(visualizeProperty)
            .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Long> placeDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Place Delay")
            .value(200L)
            .max(2000)
            .parent(autoPlaceProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Float> placeDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Place Distance")
            .value(3.5f)
            .min(1)
            .max(6)
            .inc(0.1f)
            .parent(autoPlaceProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Integer> explodeDistanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Explode Distance")
            .value(5)
            .min(2)
            .max(6)
            .build();

    private final StopWatch placeStopWatch = new StopWatch();
    private final StopWatch attackStopWatch = new StopWatch();
    private BlockPos placePos;

    public AnchorAura() {
        super(Category.COMBAT, "Automatically place/charge/explode respawn anchors near players");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(modeProperty.value());
        if (WorldHelper.INSTANCE.getDimensionID().toString().equalsIgnoreCase("the_nether"))
            return;
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (attackStopWatch.hasPassed(attackDelayProperty.value())) {
                BlockPos chargedAnchor = getChargedAnchor(Wrapper.INSTANCE.getLocalPlayer());
                if (chargedAnchor != null && shouldExplode(chargedAnchor)) {
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(chargedAnchor.getX(), chargedAnchor.getY(), chargedAnchor.getZ()), Direction.UP, chargedAnchor, false));
                    if (swingProperty.value()) {
                    Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                    }
                    attackStopWatch.reset();
                    return;
                }
                BlockPos anchor = getAnchor(Wrapper.INSTANCE.getLocalPlayer());
                if (anchor != null && shouldExplode(anchor) && !Wrapper.INSTANCE.getLocalPlayer().isSneaking()) {
                    int glowstone = InventoryHelper.INSTANCE.getFromHotbar(Items.GLOWSTONE);
                    if (glowstone != -1) {
                        int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                        InventoryHelper.INSTANCE.setSlot(glowstone, true, true);
                        Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(anchor.getX(), anchor.getY(), anchor.getZ()), Direction.UP, anchor, false));
                        if (swingProperty.value()) {
                        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                        attackStopWatch.reset();
                        return;
                    }
                }
            }
            if (placeStopWatch.hasPassed(placeDelayProperty.value()))
                if (autoPlaceProperty.value() && ((Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.RESPAWN_ANCHOR))) {
                    for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                        if (entity instanceof PlayerEntity entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer() && !FriendHelper.INSTANCE.isFriend(entity.getDisplayName().getString())) {
                            BlockPos placingPos = getOpenBlockPos(entityPlayer);
                            if (placingPos != null) {
                                if (ClientMathHelper.INSTANCE.getDistance(entityPlayer.getPos(), new Vec3d(placingPos.getX(), placingPos.getY(), placingPos.getZ())) <= 6 && !FriendHelper.INSTANCE.isFriend(entityPlayer.getName().getString()) && entityPlayer.getHealth() > 0 && shouldExplode(placingPos)) {
                                    RotationVector rotation = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(getOpenBlockPos(entityPlayer).down().getX(), getOpenBlockPos(entityPlayer).down().getY(), getOpenBlockPos(entityPlayer).down().getZ()).add(new Vec3d(0.5, 0.5, 0.5)));
                                    event.setRotation(rotation);
                                    placePos = placingPos;
                                    placeStopWatch.reset();
                                    return;
                                }
                            }
                        }
                    }
                }
        } else if (event.getMode() == EventPlayerPackets.Mode.POST) {
            if (placePos != null) {
                PlayerHelper.INSTANCE.placeBlockInPos(placePos, Hand.MAIN_HAND, true);
                placePos = null;
            }
        }
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.getDimensionID().toString().equalsIgnoreCase("the_nether"))
            return;
        if (autoPlaceProperty.value() && visualizeProperty.value())
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof PlayerEntity entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer()) {
                    BlockPos placingPos = getOpenBlockPos(entityPlayer);
                    if (placingPos != null && !FriendHelper.INSTANCE.isFriend(entityPlayer.getDisplayName().getString())) {
                        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(placingPos.getX(), placingPos.getY(), placingPos.getZ());
                        Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, shouldExplode(placingPos) ? placingColorProperty.value().getRGB() : thinkingColorProperty.value().getRGB());
                    }
                }
            });
    });

    public BlockPos getOpenBlockPos(PlayerEntity entityPlayer) {
        double distance = 6;
        BlockPos closest = null;
        for (int x = -4; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);
                    EndCrystalEntity fakeCrystal = new EndCrystalEntity(Wrapper.INSTANCE.getWorld(), pos.getX(), pos.getY(), pos.getZ());

                    if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable() && entityPlayer.canSee(fakeCrystal) && Wrapper.INSTANCE.getLocalPlayer().canSee(fakeCrystal)) {
                        BlockPos below = pos.down();
                        if (!Wrapper.INSTANCE.getWorld().getBlockState(below).getMaterial().isReplaceable()) {
                            if (!isBlocking(pos, entityPlayer)) {
                                if (onlyShowPlacementsProperty.value() && !shouldExplode(pos))
                                    continue;
                                double playerdist = entityPlayer.distanceTo(fakeCrystal);
                                double distToMe = Wrapper.INSTANCE.getLocalPlayer().distanceTo(fakeCrystal);
                                if (playerdist < distance && distToMe < placeDistanceProperty.value()) {
                                    closest = pos;
                                    distance = playerdist;
                                }
                            }
                        }
                    }
                }
            }
        }
        return closest;
    }

    private BlockPos getChargedAnchor(PlayerEntity entityPlayer) {
        double distance = 6;
        BlockPos closest = null;
        for (int x = -explodeDistanceProperty.value(); x < explodeDistanceProperty.value(); x++) {
            for (int y = -explodeDistanceProperty.value(); y < explodeDistanceProperty.value(); y++) {
                for (int z = -explodeDistanceProperty.value(); z < explodeDistanceProperty.value(); z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);

                    if (isAnchor(pos) && isChargedAnchor(pos)) {
                        if (onlyShowPlacementsProperty.value() && !shouldExplode(pos))
                            continue;
                        double playerdist = ClientMathHelper.INSTANCE.getDistance(entityPlayer.getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        double distToMe = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        if (playerdist < distance && distToMe < explodeDistanceProperty.value()) {
                            closest = pos;
                            distance = playerdist;
                        }
                    }
                }
            }
        }
        return closest;
    }

    private BlockPos getAnchor(PlayerEntity entityPlayer) {
        double distance = 6;
        BlockPos closest = null;
        for (int x = -explodeDistanceProperty.value(); x < explodeDistanceProperty.value(); x++) {
            for (int y = -explodeDistanceProperty.value(); y < explodeDistanceProperty.value(); y++) {
                for (int z = -explodeDistanceProperty.value(); z < explodeDistanceProperty.value(); z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);

                    if (isAnchor(pos) && !isChargedAnchor(pos)) {
                        if (onlyShowPlacementsProperty.value() && !shouldExplode(pos))
                            continue;
                        double playerdist = ClientMathHelper.INSTANCE.getDistance(entityPlayer.getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        double distToMe = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        if (playerdist < distance && distToMe < explodeDistanceProperty.value()) {
                            closest = pos;
                            distance = playerdist;
                        }
                    }
                }
            }
        }
        return closest;
    }

    private boolean isBlocking(BlockPos blockPos, PlayerEntity playerEntity) {
        Box box = new Box(blockPos.up());
        return playerEntity.getBoundingBox().intersects(box) || Wrapper.INSTANCE.getLocalPlayer().getBoundingBox().intersects(box);
    }

    public boolean shouldExplode(BlockPos blockPos) {
        float minDistance = 0;
        float range = explodeDistanceProperty.value();
        switch (modeProperty.value()) {
            case RISKY:
                minDistance = 4.5f;
                break;
            case SAFE:
                minDistance = 8;
                break;
        }

        if (Wrapper.INSTANCE.getLocalPlayer().getY() <= (blockPos.getY() - 1))
            minDistance = 0;

        if (!EntityHelper.INSTANCE.canSee(Wrapper.INSTANCE.getLocalPlayer(), blockPos)) {
            range = 3;
            minDistance = 0;
        }

        if (attackModeProperty.value() == AttackMode.ANY)
            return ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())) >= minDistance && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= range;
        else {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities())
                if (entity instanceof LivingEntity && isTarget((LivingEntity) entity, blockPos)) {
                    return ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())) >= minDistance && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= range;
                }
        }
        return false;
    }

    public boolean isTarget(LivingEntity livingEntity, BlockPos blockPos) {
        if (livingEntity instanceof PlayerEntity && livingEntity != Wrapper.INSTANCE.getLocalPlayer()) {
            return !FriendHelper.INSTANCE.isFriend(livingEntity.getName().getString()) && ClientMathHelper.INSTANCE.getDistance(livingEntity.getPos(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= 6 && livingEntity.getHealth() > 0;
        }
        return false;
    }

    private boolean isAnchor(BlockPos blockPos) {
        return WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.RESPAWN_ANCHOR;
    }

    private boolean isChargedAnchor(BlockPos blockPos) {
        if (!isAnchor(blockPos))
            return false;
        try {
            return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).get(RespawnAnchorBlock.CHARGES) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public enum TargetMode {
        SUICIDAL, RISKY, SAFE
    }

    public enum AttackMode {
        ANY, NEAR_TARGET
    }
}
