package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
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

@Feat(name = "AutoTrap", category = FeatureCategory.COMBAT, description = "Automatically trap people in boxes of obsidian")
public class AutoTrap extends Feature {

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

                    BlockPos above = player.getBlockPos().up().up();
                    BlockPos north = player.getBlockPos().north();
                    BlockPos east = player.getBlockPos().east();
                    BlockPos south = player.getBlockPos().south();
                    BlockPos west = player.getBlockPos().west();
                    BlockPos northUP = player.getBlockPos().north().up();
                    BlockPos eastUP = player.getBlockPos().east().up();
                    BlockPos southUP = player.getBlockPos().south().up();
                    BlockPos westUP = player.getBlockPos().west().up();

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
                                stage++;
                                break;
                            case 4:
                                if (Wrapper.INSTANCE.getWorld().getBlockState(northUP).getMaterial().isReplaceable()) {
                                    Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(northUP.getX(), northUP.getY(), northUP.getZ()), Direction.UP, northUP, false));
                                    Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                    timer.reset();
                                }
                                stage++;
                                break;
                            case 5:
                                if (Wrapper.INSTANCE.getWorld().getBlockState(eastUP).getMaterial().isReplaceable()) {
                                    Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(eastUP.getX(), eastUP.getY(), eastUP.getZ()), Direction.UP, eastUP, false));
                                    Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                    timer.reset();
                                }
                                stage++;
                                break;
                            case 6:
                                if (Wrapper.INSTANCE.getWorld().getBlockState(southUP).getMaterial().isReplaceable()) {
                                    Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(southUP.getX(), southUP.getY(), southUP.getZ()), Direction.UP, southUP, false));
                                    Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                    timer.reset();
                                }
                                stage++;
                                break;
                            case 7:
                                if (Wrapper.INSTANCE.getWorld().getBlockState(westUP).getMaterial().isReplaceable()) {
                                    Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(westUP.getX(), westUP.getY(), westUP.getZ()), Direction.UP, westUP, false));
                                    Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                                    timer.reset();
                                }
                                stage++;
                                break;
                            case 8:
                                if (Wrapper.INSTANCE.getWorld().getBlockState(above).getMaterial().isReplaceable()) {
                                    Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(above.getX(), above.getY(), above.getZ()), Direction.UP, above, false));
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
                        if (Wrapper.INSTANCE.getWorld().getBlockState(northUP).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(northUP.getX(), northUP.getY(), northUP.getZ()), Direction.UP, northUP, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (Wrapper.INSTANCE.getWorld().getBlockState(eastUP).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(eastUP.getX(), eastUP.getY(), eastUP.getZ()), Direction.UP, eastUP, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (Wrapper.INSTANCE.getWorld().getBlockState(southUP).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(southUP.getX(), southUP.getY(), southUP.getZ()), Direction.UP, southUP, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (Wrapper.INSTANCE.getWorld().getBlockState(westUP).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(westUP.getX(), westUP.getY(), westUP.getZ()), Direction.UP, westUP, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        }
                        if (Wrapper.INSTANCE.getWorld().getBlockState(above).getMaterial().isReplaceable()) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, new BlockHitResult(new Vec3d(above.getX(), above.getY(), above.getZ()), Direction.UP, above, false));
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
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
            BlockPos above = player.getBlockPos().up().up();
            BlockPos north = player.getBlockPos().north();
            BlockPos east = player.getBlockPos().east();
            BlockPos south = player.getBlockPos().south();
            BlockPos west = player.getBlockPos().west();
            BlockPos northUP = player.getBlockPos().north().up();
            BlockPos eastUP = player.getBlockPos().east().up();
            BlockPos southUP = player.getBlockPos().south().up();
            BlockPos westUP = player.getBlockPos().west().up();
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
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(northUP).getMaterial().isReplaceable()) {
                blockPos = northUP;
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(eastUP).getMaterial().isReplaceable()) {
                blockPos = eastUP;
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(southUP).getMaterial().isReplaceable()) {
                blockPos = southUP;
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(westUP).getMaterial().isReplaceable()) {
                blockPos = westUP;
            } else
            if (Wrapper.INSTANCE.getWorld().getBlockState(above).getMaterial().isReplaceable()) {
                blockPos = above;
            }
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

}
