package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.events.core.annotate.EventPointer;
import java.awt.*;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Automatically place/charge/explode respawn anchors near players")
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

    private StopWatch placeStopWatch = new StopWatch();
    private StopWatch attackStopWatch = new StopWatch();
    private BlockPos placePos;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(mode);
        if (WorldHelper.INSTANCE.getDimensionID().toString().equalsIgnoreCase("the_nether"))
            return;
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (attackStopWatch.hasPassed(attackDelay)) {
                BlockPos chargedAnchor = getChargedAnchor(Wrapper.INSTANCE.getLocalPlayer());
                if (chargedAnchor != null && shouldExplode(chargedAnchor)) {
                    Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(chargedAnchor.getX(), chargedAnchor.getY(), chargedAnchor.getZ()), Direction.UP, chargedAnchor, false));
                    Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
                    attackStopWatch.reset();
                    return;
                }
                BlockPos anchor = getAnchor(Wrapper.INSTANCE.getLocalPlayer());
                if (anchor != null && shouldExplode(anchor) && !Wrapper.INSTANCE.getLocalPlayer().isShiftKeyDown()) {
                    int glowstone = InventoryHelper.INSTANCE.getFromHotbar(Items.GLOWSTONE);
                    if (glowstone != -1) {
                        int savedSlot = InventoryHelper.INSTANCE.getInventory().selected;
                        InventoryHelper.INSTANCE.setSlot(glowstone, true, true);
                        Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(anchor.getX(), anchor.getY(), anchor.getZ()), Direction.UP, anchor, false));
                        Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
                        InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                        attackStopWatch.reset();
                        return;
                    }
                }
            }
            if (placeStopWatch.hasPassed(delay))
                if (autoPlace && ((Wrapper.INSTANCE.getLocalPlayer().getMainHandItem() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandItem().getItem() == Items.RESPAWN_ANCHOR))) {
                    for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
                        if (entity instanceof Player entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer() && !FriendHelper.INSTANCE.isFriend(entity.getDisplayName().getString())) {
                            BlockPos placingPos = getOpenBlockPos(entityPlayer);
                            if (placingPos != null) {
                                if (ClientMathHelper.INSTANCE.getDistance(entityPlayer.position(), new Vec3(placingPos.getX(), placingPos.getY(), placingPos.getZ())) <= 6 && !FriendHelper.INSTANCE.isFriend(entityPlayer.getName().getString()) && entityPlayer.getHealth() > 0 && shouldExplode(placingPos)) {
                                    RotationVector rotation = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3(getOpenBlockPos(entityPlayer).below().getX(), getOpenBlockPos(entityPlayer).below().getY(), getOpenBlockPos(entityPlayer).below().getZ()).add(new Vec3(0.5, 0.5, 0.5)));
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
                PlayerHelper.INSTANCE.placeBlockInPos(placePos, InteractionHand.MAIN_HAND, true);
                placePos = null;
            }
        }
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.getDimensionID().toString().equalsIgnoreCase("the_nether"))
            return;
        if (autoPlace && visualize)
            Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
                if (entity instanceof Player entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer()) {
                    BlockPos placingPos = getOpenBlockPos(entityPlayer);
                    if (placingPos != null && !FriendHelper.INSTANCE.isFriend(entityPlayer.getDisplayName().getString())) {
                        Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(placingPos.getX(), placingPos.getY(), placingPos.getZ());
                        AABB box = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, shouldExplode(placingPos) ? placingColor : thinkingColor);
                    }
                }
            });
    });

    public BlockPos getOpenBlockPos(Player entityPlayer) {
        double distance = 6;
        BlockPos closest = null;
        for (int x = -4; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);
                    EndCrystal fakeCrystal = new EndCrystal(Wrapper.INSTANCE.getWorld(), pos.getX(), pos.getY(), pos.getZ());

                    if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable() && entityPlayer.hasLineOfSight(fakeCrystal) && Wrapper.INSTANCE.getLocalPlayer().hasLineOfSight(fakeCrystal)) {
                        BlockPos below = pos.below();
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

    private BlockPos getChargedAnchor(Player entityPlayer) {
        double distance = 6;
        BlockPos closest = null;
        for (int x = -explodeDistance; x < explodeDistance; x++) {
            for (int y = -explodeDistance; y < explodeDistance; y++) {
                for (int z = -explodeDistance; z < explodeDistance; z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);

                    if (isAnchor(pos) && isChargedAnchor(pos)) {
                        if (onlyShowPlacements && !shouldExplode(pos))
                            continue;
                        double playerdist = ClientMathHelper.INSTANCE.getDistance(entityPlayer.position(), new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                        double distToMe = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(pos.getX(), pos.getY(), pos.getZ()));
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

    private BlockPos getAnchor(Player entityPlayer) {
        double distance = 6;
        BlockPos closest = null;
        for (int x = -explodeDistance; x < explodeDistance; x++) {
            for (int y = -explodeDistance; y < explodeDistance; y++) {
                for (int z = -explodeDistance; z < explodeDistance; z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);

                    if (isAnchor(pos) && !isChargedAnchor(pos)) {
                        if (onlyShowPlacements && !shouldExplode(pos))
                            continue;
                        double playerdist = ClientMathHelper.INSTANCE.getDistance(entityPlayer.position(), new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                        double distToMe = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(pos.getX(), pos.getY(), pos.getZ()));
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

    private boolean isBlocking(BlockPos blockPos, Player playerEntity) {
        AABB box = new AABB(blockPos.above());
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
            return ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ())) >= minDistance && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= range;
        else {
            for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering())
                if (entity instanceof LivingEntity && isTarget((LivingEntity) entity, blockPos)) {
                    return ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ())) >= minDistance && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= range;
                }
        }
        return false;
    }

    public boolean isTarget(LivingEntity livingEntity, BlockPos blockPos) {
        if (livingEntity instanceof Player && livingEntity != Wrapper.INSTANCE.getLocalPlayer()) {
            return !FriendHelper.INSTANCE.isFriend(livingEntity.getName().getString()) && ClientMathHelper.INSTANCE.getDistance(livingEntity.position(), new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= 6 && livingEntity.getHealth() > 0;
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
            return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getValue(RespawnAnchorBlock.CHARGE) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
