package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;

@Feat(name = "CrystalAura", category = FeatureCategory.COMBAT, description = "Auto place/destroy End Crystals")
public class CrystalAura extends Feature {

    @Op(name = "Mode", all = {"Suicidal", "Risky", "Safe"})
    public String mode = "Suicidal";

    @Op(name = "Attack", all = {"Any", "Near Target"})
    public String attackMode = "Any";

    @OpChild(name = "Max Attack Distance", min = 1, max = 6, inc = 0.1f, parent = "Attack")
    public float attackDistance = 4;

    @Op(name = "Auto Place")
    public boolean autoPlace = false;

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

    private Timer timer = new Timer();
    private BlockPos placePos;

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            this.setSuffix(mode);
            if (placePos != null) {
                RotationVector rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()).add(new Vec3d(0.5, 0.5, 0.5)));
                event.setRotation(rotation);
            }

            if (timer.hasPassed(delay))
                if (autoPlace && ((Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.END_CRYSTAL))) {
                    Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                        if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer() && !Friend.isFriend(entity.getDisplayName().asString())) {
                            PlayerEntity entityPlayer = (PlayerEntity) entity;
                            BlockPos placingPos = getOpenBlockPos(entityPlayer);
                            if (placingPos != null) {
                                EndCrystalEntity crystal = new EndCrystalEntity(Wrapper.INSTANCE.getWorld(), placingPos.getX(), placingPos.getY(), placingPos.getZ());
                                if (entityPlayer.distanceTo(crystal) <= 6 && Wrapper.INSTANCE.getLocalPlayer().distanceTo(crystal) <= 6 && !Friend.isFriend(entityPlayer.getName().getString()) && entityPlayer.getHealth() > 0 && shouldAttack(crystal)) {
                                    RotationVector rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(getOpenBlockPos(entityPlayer).down().getX(), getOpenBlockPos(entityPlayer).down().getY(), getOpenBlockPos(entityPlayer).down().getZ()).add(new Vec3d(0.5, 0.5, 0.5)));
                                    event.setRotation(rotation);
                                    placePos = placingPos.down();
                                    timer.reset();
                                    return;
                                }
                            }
                        }
                    });
                }
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof EndCrystalEntity) {
                    EndCrystalEntity enderCrystalEntity = (EndCrystalEntity) entity;
                    if (shouldAttack(enderCrystalEntity)) {
                        RotationVector rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), enderCrystalEntity);
                        event.setRotation(rotation);
                        Wrapper.INSTANCE.getInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), enderCrystalEntity);
                        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND.MAIN_HAND);
                    }
                }
            });
        } else {
            if (placePos != null) {
                BlockHitResult blockHitResult = new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), Direction.UP, placePos, false);
                NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult));
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                placePos = null;
            }
        }
    }

    @EventListener(events = {EventRender3D.class})
    public void run(EventRender3D event) {
        if (autoPlace && visualize)
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer()) {
                    PlayerEntity entityPlayer = (PlayerEntity) entity;
                    BlockPos placingPos = getOpenBlockPos(entityPlayer);
                    if (placingPos != null && !Friend.isFriend(entityPlayer.getDisplayName().asString())) {
                        EndCrystalEntity crystal = new EndCrystalEntity(Wrapper.INSTANCE.getWorld(), placingPos.getX(), placingPos.getY(), placingPos.getZ());
                        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(placingPos.getX(), placingPos.getY(), placingPos.getZ());
                        Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBox(event.getMatrixStack(), box, shouldAttack(crystal) ? placingColor : thinkingColor);
                    }
                }
            });
    }

    public BlockPos getOpenBlockPos(PlayerEntity entityPlayer) {
        double distance = 6;
        BlockPos closest = null;
        for (int x = -4; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);
                    EndCrystalEntity fakeCrystal = new EndCrystalEntity(Wrapper.INSTANCE.getWorld(), pos.getX(), pos.getY(), pos.getZ());

                    List<Entity> list = Wrapper.INSTANCE.getWorld().getOtherEntities((Entity) null, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D));
                    boolean collides = !list.isEmpty();
                    if (WorldHelper.INSTANCE.getBlock(pos) == Blocks.AIR && !collides && entityPlayer.canSee(fakeCrystal) && Wrapper.INSTANCE.getLocalPlayer().canSee(fakeCrystal)) {
                        BlockPos below = pos.down();
                        Block belowBlock = WorldHelper.INSTANCE.getBlock(below);
                        if (belowBlock == Blocks.OBSIDIAN || belowBlock == Blocks.BEDROCK) {
                            if (!isBlocking(pos, entityPlayer)) {
                                if (onlyShowPlacements && !shouldAttack(fakeCrystal))
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

    private boolean isBlocking(BlockPos blockPos, PlayerEntity EntityPlayer) {
        Box box = new Box(blockPos.up());
        if (EntityPlayer.getBoundingBox().intersects(box))
            return true;
        return false;
    }

    public boolean shouldAttack(EndCrystalEntity enderCrystalEntity) {
        float minDistance = 0;
        float range = attackDistance;
        switch (mode) {
            case "Risky":
                minDistance = 4.5f;
                break;
            case "Safe":
                minDistance = 8;
                break;
        }

        if (Wrapper.INSTANCE.getLocalPlayer().getY() <= (enderCrystalEntity.getY() - 1))
            minDistance = 0;

        if (!Wrapper.INSTANCE.getLocalPlayer().canSee(enderCrystalEntity)) {
            range = 3;
            minDistance = 0;
        }

        if (attackMode.equalsIgnoreCase("Any"))
            return Wrapper.INSTANCE.getLocalPlayer().distanceTo(enderCrystalEntity) >= minDistance && Wrapper.INSTANCE.getLocalPlayer().distanceTo(enderCrystalEntity) <= range;
        else {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities())
                if (entity instanceof LivingEntity && isTarget((LivingEntity) entity, enderCrystalEntity)) {
                    return Wrapper.INSTANCE.getLocalPlayer().distanceTo(enderCrystalEntity) >= minDistance && Wrapper.INSTANCE.getLocalPlayer().distanceTo(enderCrystalEntity) <= range;
                }
        }
        return false;
    }

    public boolean isTarget(LivingEntity livingEntity, EndCrystalEntity enderCrystalEntity) {
        if (livingEntity instanceof PlayerEntity && livingEntity != Wrapper.INSTANCE.getLocalPlayer()) {
            return !Friend.isFriend(livingEntity.getName().getString()) && livingEntity.distanceTo(enderCrystalEntity) <= 6 && livingEntity.getHealth() > 0;
        }
        return false;
    }

}
