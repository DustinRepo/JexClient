package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

@Feat(name = "AutoTrap", category = FeatureCategory.COMBAT, description = "Automatically trap people in boxes of obsidian")
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
    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets) {
            EventPlayerPackets eventPlayerPackets = (EventPlayerPackets)event;
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (!timer.hasPassed(placeDelay))
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
                    InventoryHelper.INSTANCE.getInventory().selectedSlot = obby;

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
                        BlockPos pos = placePos.get(stage);
                        if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                            RotationVector rotationVector = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), ClientMathHelper.INSTANCE.getVec(pos));
                            if (rotate)
                                ((EventPlayerPackets) event).setRotation(rotationVector);
                            timer.reset();
                        }
                        stage++;
                        if (stage == placePos.size())
                        {
                            InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                            this.setState(false);
                        }
                    } else {
                        for (BlockPos pos : placePos) {
                            if (Wrapper.INSTANCE.getWorld().getBlockState(pos).getMaterial().isReplaceable()) {
                                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false));
                                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                            }
                        }
                        InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                        this.setState(false);
                        timer.reset();
                        this.stage = 0;
                    }
                }
            }
        } else if (event instanceof EventRender3D) {
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
        }
    }

    private PlayerEntity getPlayerToTrap() {
        PlayerEntity playerEntity = null;
        float distance = targetDistance;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof PlayerEntity && !Friend.isFriend(entity.getName().asString()) && entity != Wrapper.INSTANCE.getLocalPlayer()) {
                if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) < distance && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > 2) {
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
