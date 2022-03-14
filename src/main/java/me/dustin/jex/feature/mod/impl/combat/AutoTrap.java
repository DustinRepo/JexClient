package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Automatically trap people in boxes of obsidian")
public class AutoTrap extends Feature {

    @Op(name = "Rotate")
    public boolean rotate = true;
    @Op(name = "Target Distance", min = 2, max = 6, inc = 0.1f)
    public float targetDistance = 6;
    @Op(name = "Place Delay (MS)", min = 0, max = 250)
    public int placeDelay = 0;
    @Op(name = "Place Color", isColor = true)
    public int placeColor = 0xffff0000;

    private int stage = 0;
    private StopWatch stopWatch = new StopWatch();
    private BlockPos placingPos;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (placingPos != null) {
            RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPlacingLookPos(placingPos, true));
            if (rotate)
                ((EventPlayerPackets) event).setRotation(rotationVector);
            PlayerHelper.INSTANCE.placeBlockInPos(placingPos, Hand.MAIN_HAND, true);
            placingPos = null;
        }
        if (!stopWatch.hasPassed(placeDelay))
            return;
        int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
        int obby = InventoryHelper.INSTANCE.getFromHotbar(Items.OBSIDIAN);
        if (obby == -1) {
            this.stage = 0;
            this.setState(false);
            return;
        }
        PlayerEntity player = getPlayerToTrap();
        if (player != null) {
            InventoryHelper.INSTANCE.setSlot(obby, true, true);

            ArrayList<BlockPos> placePos = new ArrayList<>();
            placePos.add(player.getBlockPos().north());
            placePos.add(player.getBlockPos().east());
            placePos.add(player.getBlockPos().south());
            placePos.add(player.getBlockPos().west());
            placePos.add(player.getBlockPos().north().up());
            placePos.add(player.getBlockPos().east().up());
            placePos.add(player.getBlockPos().south().up());
            placePos.add(player.getBlockPos().west().up());
            placePos.add(player.getBlockPos().up().up());
            if (placeDelay != 0) {
                if (stage == placePos.size()) {
                    InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                    this.setState(false);
                    return;
                }
                BlockPos pos = placePos.get(stage);
                if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
                    RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPlacingLookPos(pos, true));
                    if (rotate)
                        ((EventPlayerPackets) event).setRotation(rotationVector);
                    placingPos = pos;
                    stopWatch.reset();
                }
                stage++;
            } else {
                for (BlockPos pos : placePos) {
                    if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
                        PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, true);
                    }
                }

                InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                this.setState(false);
                stopWatch.reset();
                this.stage = 0;
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        PlayerEntity player = getPlayerToTrap();
        if (player == null)return;
        ArrayList<BlockPos> placePos = new ArrayList<>();
        placePos.add(player.getBlockPos().north());
        placePos.add(player.getBlockPos().east());
        placePos.add(player.getBlockPos().south());
        placePos.add(player.getBlockPos().west());
        placePos.add(player.getBlockPos().north().up());
        placePos.add(player.getBlockPos().east().up());
        placePos.add(player.getBlockPos().south().up());
        placePos.add(player.getBlockPos().west().up());
        placePos.add(player.getBlockPos().up().up());
        BlockPos blockPos = null;
        for (BlockPos pos : placePos) {
            if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
                blockPos = pos;
                break;
            }
        }
        if (blockPos == null)
            return;
        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
        Box bb = new Box(renderPos.getX(), renderPos.getY(), renderPos.getZ(), renderPos.getX() + 1, renderPos.getY() + 1, renderPos.getZ() + 1);
        Render3DHelper.INSTANCE.drawBox(((EventRender3D) event).getMatrixStack(), bb, placeColor);
    });

    private PlayerEntity getPlayerToTrap() {
        PlayerEntity playerEntity = null;
        float distance = targetDistance;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof PlayerEntity && !FriendHelper.INSTANCE.isFriend(entity.getName().asString()) && entity != Wrapper.INSTANCE.getLocalPlayer()) {
                if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) < distance && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > 2 && !Wrapper.INSTANCE.getWorld().isOutOfHeightLimit((int)entity.getY())) {
                    playerEntity = (PlayerEntity)entity;
                    distance = Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity);
                }
            }
        }
        return playerEntity;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.stage = 0;
    }
}
