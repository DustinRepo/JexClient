package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
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

import java.awt.*;

@Feat(name = "AnchorAura", category = FeatureCategory.COMBAT, description = "Automatically place/charge/explode respawn anchors near players")
public class AnchorAura extends Feature {

    @Op(name = "Mode", all = {"Suicidal", "Risky", "Safe"})
    public String mode = "Suicidal";
    @Op(name = "Explode", all = {"Any", "Near Target"})
    public String attackMode = "Any";
    @Op(name = "Attack Delay", min = 0, max = 2000)
    public int attackDelay = 200;
    @Op(name = "Auto Place")
    public boolean autoPlace = false;
    @Op(name = "Explode Distance", min = 2, max = 6)
    public int explodeDistance = 5;

    @OpChild(name = "Visualize", parent = "Auto Place")
    public boolean visualize = true;
    @OpChild(name = "Only show placements", parent = "Visualize")
    public boolean onlyShowPlacements;
    @OpChild(name = "Thinking Color", isColor = true, parent = "Visualize")
    public int thinkingColor = new Color(0, 150, 255).getRGB();
    @OpChild(name = "Placing Color", isColor = true, parent = "Visualize")
    public int placingColor = new Color(255, 0, 0).getRGB();

    @OpChild(name = "Place Delay", min = 0, max = 2000, parent = "Auto Place")
    public int delay = 200;

    @OpChild(name = "Place Distance", min = 1, max = 6, inc = 0.1f, parent = "Auto Place")
    public float placeDistance = 3.5f;

    private Timer placeTimer = new Timer();
    private Timer attackTimer = new Timer();
    private BlockPos placePos;

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class})
    private void runMethod(Event event) {
        this.setSuffix(mode);
        if (WorldHelper.INSTANCE.getDimensionID().toString().equalsIgnoreCase("the_nether"))
            return;
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (attackTimer.hasPassed(attackDelay)) {
                    BlockPos chargedAnchor = getChargedAnchor(Wrapper.INSTANCE.getLocalPlayer());
                    if (chargedAnchor != null && shouldExplode(chargedAnchor)) {
                        Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(chargedAnchor.getX(), chargedAnchor.getY(), chargedAnchor.getZ()), Direction.UP, chargedAnchor, false));
                        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        attackTimer.reset();
                        return;
                    }
                    BlockPos anchor = getAnchor(Wrapper.INSTANCE.getLocalPlayer());
                    if (anchor != null && shouldExplode(anchor) && !Wrapper.INSTANCE.getLocalPlayer().isSneaking()) {
                        int glowstone = InventoryHelper.INSTANCE.getFromHotbar(Items.GLOWSTONE);
                        if (glowstone != -1) {
                            int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                            InventoryHelper.INSTANCE.getInventory().selectedSlot = glowstone;
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(anchor.getX(), anchor.getY(), anchor.getZ()), Direction.UP, anchor, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                            InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                            attackTimer.reset();
                            return;
                        }
                    }
                }
                if (placeTimer.hasPassed(delay))
                    if (autoPlace && ((Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.RESPAWN_ANCHOR))) {
                        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                            if (entity instanceof PlayerEntity entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer() && !Friend.isFriend(entity.getDisplayName().asString())) {
                                BlockPos placingPos = getOpenBlockPos(entityPlayer);
                                if (placingPos != null) {
                                    if (ClientMathHelper.INSTANCE.getDistance(entityPlayer.getPos(), new Vec3d(placingPos.getX(), placingPos.getY(), placingPos.getZ())) <= 6 && !Friend.isFriend(entityPlayer.getName().getString()) && entityPlayer.getHealth() > 0 && shouldExplode(placingPos)) {
                                        RotationVector rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(getOpenBlockPos(entityPlayer).down().getX(), getOpenBlockPos(entityPlayer).down().getY(), getOpenBlockPos(entityPlayer).down().getZ()).add(new Vec3d(0.5, 0.5, 0.5)));
                                        eventPlayerPackets.setRotation(rotation);
                                        placePos = placingPos;
                                        placeTimer.reset();
                                        return;
                                    }
                                }
                            }
                        }
                    }
            } else if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.POST) {
                if (placePos != null) {
                    PlayerHelper.INSTANCE.placeBlockInPos(placePos, Hand.MAIN_HAND, true);
                    placePos = null;
                }
            }
        } else if (event instanceof EventRender3D eventRender3D) {
            if (autoPlace && visualize)
                Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                    if (entity instanceof PlayerEntity entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer()) {
                        BlockPos placingPos = getOpenBlockPos(entityPlayer);
                        if (placingPos != null && !Friend.isFriend(entityPlayer.getDisplayName().asString())) {
                            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(placingPos.getX(), placingPos.getY(), placingPos.getZ());
                            Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                            Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), box, shouldExplode(placingPos) ? placingColor : thinkingColor);
                        }
                    }
                });
        }
    }

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
                                if (onlyShowPlacements && !shouldExplode(pos))
                                    continue;
                                double playerdist = entityPlayer.distanceTo(fakeCrystal);
                                double distToMe = Wrapper.INSTANCE.getLocalPlayer().distanceTo(fakeCrystal);
                                if (playerdist < distance && distToMe < placeDistance) {
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
        for (int x = -explodeDistance; x < explodeDistance; x++) {
            for (int y = -explodeDistance; y < explodeDistance; y++) {
                for (int z = -explodeDistance; z < explodeDistance; z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);

                    if (isAnchor(pos) && isChargedAnchor(pos)) {
                        if (onlyShowPlacements && !shouldExplode(pos))
                            continue;
                        double playerdist = ClientMathHelper.INSTANCE.getDistance(entityPlayer.getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        double distToMe = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        if (playerdist < distance && distToMe < explodeDistance) {
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
        for (int x = -explodeDistance; x < explodeDistance; x++) {
            for (int y = -explodeDistance; y < explodeDistance; y++) {
                for (int z = -explodeDistance; z < explodeDistance; z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);

                    if (isAnchor(pos) && !isChargedAnchor(pos)) {
                        if (onlyShowPlacements && !shouldExplode(pos))
                            continue;
                        double playerdist = ClientMathHelper.INSTANCE.getDistance(entityPlayer.getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        double distToMe = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                        if (playerdist < distance && distToMe < explodeDistance) {
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
        float range = explodeDistance;
        switch (mode) {
            case "Risky":
                minDistance = 4.5f;
                break;
            case "Safe":
                minDistance = 8;
                break;
        }

        if (Wrapper.INSTANCE.getLocalPlayer().getY() <= (blockPos.getY() - 1))
            minDistance = 0;

        if (!EntityHelper.INSTANCE.canSee(Wrapper.INSTANCE.getLocalPlayer(), blockPos)) {
            range = 3;
            minDistance = 0;
        }

        if (attackMode.equalsIgnoreCase("Any"))
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
            return !Friend.isFriend(livingEntity.getName().getString()) && ClientMathHelper.INSTANCE.getDistance(livingEntity.getPos(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= 6 && livingEntity.getHealth() > 0;
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
}
