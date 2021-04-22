package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

@Feat(name = "Surround", category = FeatureCategory.COMBAT, description = "Automatically place obsidian around your feet to defend from crystals")
public class Surround extends Feature {

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
                InventoryHelper.INSTANCE.getInventory().selectedSlot = obby;
                BlockPos north = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().north();
                BlockPos east = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().east();
                BlockPos south = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().south();
                BlockPos west = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().west();
                if (placeDelay != 0) {
                    switch (stage) {
                        case 0:
                            if (Wrapper.INSTANCE.getWorld().getBlockState(north).getMaterial().isReplaceable()) {
                                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(north.getX(), north.getY(), north.getZ()), Direction.UP, north, false));
                                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                timer.reset();
                            }
                            stage++;
                            break;
                        case 1:
                            if (Wrapper.INSTANCE.getWorld().getBlockState(east).getMaterial().isReplaceable()) {
                                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(east.getX(), east.getY(), east.getZ()), Direction.UP, east, false));
                                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                timer.reset();
                            }
                            stage++;
                            break;
                        case 2:
                            if (Wrapper.INSTANCE.getWorld().getBlockState(south).getMaterial().isReplaceable()) {
                                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(south.getX(), south.getY(), south.getZ()), Direction.UP, south, false));
                                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                timer.reset();
                            }
                            stage++;
                            break;
                        case 3:
                            if (Wrapper.INSTANCE.getWorld().getBlockState(west).getMaterial().isReplaceable()) {
                                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(west.getX(), west.getY(), west.getZ()), Direction.UP, west, false));
                                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                timer.reset();
                            }
                            stage = 0;
                            InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                            this.setState(false);
                            break;
                    }
                } else {
                        if (Wrapper.INSTANCE.getWorld().getBlockState(north).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(north.getX(), north.getY(), north.getZ()), Direction.UP, north, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (Wrapper.INSTANCE.getWorld().getBlockState(east).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(east.getX(), east.getY(), east.getZ()), Direction.UP, east, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (Wrapper.INSTANCE.getWorld().getBlockState(south).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(south.getX(), south.getY(), south.getZ()), Direction.UP, south, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (Wrapper.INSTANCE.getWorld().getBlockState(west).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(west.getX(), west.getY(), west.getZ()), Direction.UP, west, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        stage = 0;
                        InventoryHelper.INSTANCE.getInventory().selectedSlot = savedSlot;
                        this.setState(false);
                }
            }
        } else if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D)event;
            BlockPos north = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().north();
            BlockPos east = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().east();
            BlockPos south = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().south();
            BlockPos west = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().west();
            BlockPos blockPos = null;
            if (Wrapper.INSTANCE.getWorld().getBlockState(north).getMaterial().isReplaceable()) {
                blockPos = north;
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(east).getMaterial().isReplaceable()) {
                blockPos = east;
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(south).getMaterial().isReplaceable()) {
                blockPos = south;
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(west).getMaterial().isReplaceable()) {
                blockPos = west;
            }
            if (blockPos == null)
                return;
            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
            Box bb = new Box(renderPos.getX(), renderPos.getY(), renderPos.getZ(), renderPos.getX() + 1, renderPos.getY() + 1, renderPos.getZ() + 1);
            Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), bb, placeColor);
        }
    }

}
