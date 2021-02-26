package me.dustin.jex.module.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.command.CommandManager;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.player.AutoEat;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@ModClass(name = "AutoFarm", category = ModCategory.WORLD, description = "Automatically break and re-plant full crops in a selected area")
public class AutoFarm extends Module {

    @Op(name = "Break Delay", min = 0, max = 1000, inc = 10)
    public int breakDelay = 250;
    @Op(name = "Place Delay", min = 0, max = 1000, inc = 10)
    public int placeDelay = 250;

    @Op(name = "Allow Baritone Break")
    public boolean allowBaritoneBreak = true;
    @Op(name = "Allow Baritone Place")
    public boolean allowBaritonePlace = true;

    private boolean savedAllowBreak, savedAllowPlace;

    public static BlockPos pos1, pos2;
    private ArrayList<BlockPos> cropsToBreak = new ArrayList<>();
    private ArrayList<BlockPos> cropsToReplant = new ArrayList<>();
    private ArrayList<ItemEntity> itemsToGrab = new ArrayList<>();

    private Timer breakTimer = new Timer();
    private Timer placeTimer = new Timer();

    private boolean plantCrop;
    private boolean breakCrop;

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (pos1 == null || pos2 == null)
            return;
        if (event instanceof EventPlayerPackets) {
            EventPlayerPackets eventPlayerPackets = (EventPlayerPackets)event;
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (cropsToBreak.isEmpty() && cropsToReplant.isEmpty() && itemsToGrab.isEmpty())
                    searchCrops();
                    if (AutoEat.isEating)
                        return;
                    sort();
                    if (!cropsToBreak.isEmpty()) {
                        BlockPos breakPos = cropsToBreak.get(0);
                        Block block = WorldHelper.INSTANCE.getBlock(breakPos);
                        if (block == Blocks.AIR) {
                            cropsToBreak.remove(0);
                            breakCrop = false;
                            return;
                        }
                        Vec3d blockVec = new Vec3d(breakPos.getX() + 0.5, breakPos.getY() + 0.5, breakPos.getZ() + 0.5);
                        if (!BaritoneHelper.INSTANCE.isBaritoneRunning() && ClientMathHelper.INSTANCE.getDistance(PlayerHelper.INSTANCE.getPlayerVec(), blockVec) > 2) {
                            BaritoneHelper.INSTANCE.pathTo(breakPos);
                        } else {
                            //break the bitch
                            if (ClientMathHelper.INSTANCE.getDistance(PlayerHelper.INSTANCE.getPlayerVec(), blockVec) <= 2) {
                                float[] rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), blockVec);
                                eventPlayerPackets.setYaw(rotation[0]);
                                eventPlayerPackets.setPitch(rotation[1]);
                                breakCrop = true;
                            }
                        }
                    } else if (!cropsToReplant.isEmpty()) {
                        BlockPos plantPos = cropsToReplant.get(0);
                        Block block = WorldHelper.INSTANCE.getBlock(plantPos);
                        Block belowBlock = WorldHelper.INSTANCE.getBlock(plantPos);
                        if (block != Blocks.AIR && !(belowBlock == Blocks.FARMLAND || belowBlock == Blocks.SOUL_SOIL)) {
                            cropsToReplant.remove(0);
                            plantCrop = false;
                            return;
                        }
                        Vec3d blockVec = new Vec3d(plantPos.getX() + 0.5, plantPos.getY() + 0.1, plantPos.getZ() + 0.5);
                        if (!BaritoneHelper.INSTANCE.isBaritoneRunning() && ClientMathHelper.INSTANCE.getDistance(PlayerHelper.INSTANCE.getPlayerVec(), blockVec) > 2) {
                            BaritoneHelper.INSTANCE.pathTo(plantPos);

                        } else {
                            if (getSeeds() != -1) {
                                if (ClientMathHelper.INSTANCE.getDistance(PlayerHelper.INSTANCE.getPlayerVec(), blockVec) <= 2) {
                                    if (InventoryHelper.INSTANCE.getInventory().selectedSlot != getSeeds())
                                        InventoryHelper.INSTANCE.getInventory().selectedSlot = getSeeds();
                                    float[] rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), blockVec);
                                    eventPlayerPackets.setYaw(rotation[0]);
                                    eventPlayerPackets.setPitch(rotation[1]);
                                    plantCrop = true;
                                }
                            }
                        }
                    } else {
                        if (!itemsToGrab.isEmpty()) {
                            sortItems();
                            ItemEntity itemEntity = itemsToGrab.get(0);
                            if (itemEntity == null) {
                                itemsToGrab.remove(0);
                                return;
                            } else {
                                if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(itemEntity) > 1) {
                                    BaritoneHelper.INSTANCE.pathTo(itemEntity.getBlockPos());
                                } else {
                                    itemsToGrab.remove(0);
                                }
                            }
                        }
                    }
            } else {
                if (breakCrop) {
                    BlockPos breakPos = cropsToBreak.get(0);
                    Vec3d blockVec = new Vec3d(breakPos.getX() + 0.5, breakPos.getY() + 0.1, breakPos.getZ() + 0.5);
                    float[] rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), blockVec);
                    float[] savedRotation = {Wrapper.INSTANCE.getLocalPlayer().yaw, Wrapper.INSTANCE.getLocalPlayer().pitch};
                    Wrapper.INSTANCE.getLocalPlayer().yaw = rotation[0];
                    Wrapper.INSTANCE.getLocalPlayer().pitch = rotation[1];
                    HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().raycast(3.5f, Wrapper.INSTANCE.getMinecraft().getTickDelta(), false);
                    Wrapper.INSTANCE.getLocalPlayer().yaw = savedRotation[0];
                    Wrapper.INSTANCE.getLocalPlayer().pitch = savedRotation[1];
                    if (hitResult instanceof BlockHitResult) {
                        if (breakTimer.hasPassed(breakDelay)) {
                            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                            Wrapper.INSTANCE.getInteractionManager().attackBlock(breakPos, ((BlockHitResult) hitResult).getSide());
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                            cropsToBreak.remove(0);
                            breakTimer.reset();
                            breakCrop = false;
                        }
                    }
                } else if (plantCrop) {
                    BlockPos plantPos = cropsToReplant.get(0);
                    Vec3d blockVec = new Vec3d(plantPos.getX() + 0.5, plantPos.getY() + 0.1, plantPos.getZ() + 0.5);
                    float[] rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), blockVec);
                    float[] savedRotation = {Wrapper.INSTANCE.getLocalPlayer().yaw, Wrapper.INSTANCE.getLocalPlayer().pitch};
                    Wrapper.INSTANCE.getLocalPlayer().yaw = rotation[0];
                    Wrapper.INSTANCE.getLocalPlayer().pitch = rotation[1];
                    HitResult hitResult = Wrapper.INSTANCE.getLocalPlayer().raycast(3.5f, Wrapper.INSTANCE.getMinecraft().getTickDelta(), false);
                    Wrapper.INSTANCE.getLocalPlayer().yaw = savedRotation[0];
                    Wrapper.INSTANCE.getLocalPlayer().pitch = savedRotation[1];

                    if (hitResult instanceof BlockHitResult) {
                        BlockHitResult sentResult = new BlockHitResult(blockVec.add(0, -1.1f, 0), Direction.UP, plantPos.add(0, -1, 0), false);
                        if (placeTimer.hasPassed(placeDelay)) {
                            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, (BlockHitResult)sentResult);
                            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                            placeTimer.reset();
                            plantCrop = false;
                        }
                    }
                }
            }
        } else if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D)event;
            if (!cropsToBreak.isEmpty()) {
                for (BlockPos blockPos : cropsToBreak) {
                    Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
                    Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 0.1f, renderPos.z + 1);
                    Render3DHelper.INSTANCE.drawBox(box, 0xffff0000);
                }
            }
            if (!cropsToReplant.isEmpty()) {
                for (BlockPos blockPos : cropsToReplant) {
                    Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
                    Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 0.1f, renderPos.z + 1);
                    Render3DHelper.INSTANCE.drawBox(box, 0xff00ff00);
                }
            }
        }
    }

    private int getSeeds() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.BEETROOT_SEEDS || stack.getItem() == Items.CARROT || stack.getItem() == Items.POTATO) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            this.savedAllowBreak = BaritoneHelper.INSTANCE.getAllowBreak();
            this.savedAllowPlace = BaritoneHelper.INSTANCE.getAllowPlace();
        } else {
            this.setState(false);
            ChatHelper.INSTANCE.addClientMessage("Sorry, but this mod requires Baritone to run.");
            return;
        }
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            if (pos1 == null || pos2 == null) {
                ChatHelper.INSTANCE.addClientMessage("Positions not found. Please set positions with " + CommandManager.INSTANCE.getPrefix() + "autofarm pos1/pos2 to set a position to your current position");
            }
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (BaritoneHelper.INSTANCE.baritoneExists()) {
            BaritoneHelper.INSTANCE.setAllowBreak(this.savedAllowBreak);
            BaritoneHelper.INSTANCE.setAllowPlace(this.savedAllowPlace);
            this.cropsToReplant.clear();
            this.cropsToBreak.clear();
        }
        super.onDisable();
    }

    private void sort() {
        Collections.sort(cropsToBreak, new Comparator<BlockPos>() {
            public int compare(BlockPos mod, BlockPos mod1) {
                if (mod.getManhattanDistance(new Vec3i((int) Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ())) > mod1.getManhattanDistance(new Vec3i((int)Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ()))) {
                    return -1;
                }
                if(mod.getManhattanDistance(new Vec3i((int)Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ())) < mod1.getManhattanDistance(new Vec3i((int)Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ()))) {
                    return 1;
                }
                return 0;
            }
        });
        Collections.sort(cropsToReplant, new Comparator<BlockPos>() {
            public int compare(BlockPos mod, BlockPos mod1) {
                if (mod.getManhattanDistance(new Vec3i((int)Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ())) > mod1.getManhattanDistance(new Vec3i((int)Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ()))) {
                    return -1;
                }
                if(mod.getManhattanDistance(new Vec3i((int)Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ())) < mod1.getManhattanDistance(new Vec3i((int)Wrapper.INSTANCE.getLocalPlayer().getX(), (int)Wrapper.INSTANCE.getLocalPlayer().getY(), (int)Wrapper.INSTANCE.getLocalPlayer().getZ()))) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void sortItems() {
        Collections.sort(itemsToGrab, new Comparator<ItemEntity>() {
            public int compare(ItemEntity mod, ItemEntity mod1) {
                if (mod.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) > mod1.distanceTo(Wrapper.INSTANCE.getLocalPlayer())) {
                    return -1;
                }
                if(mod.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) < mod1.distanceTo(Wrapper.INSTANCE.getLocalPlayer())) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void searchCrops() {
        cropsToBreak.clear();
        cropsToReplant.clear();
        for (BlockPos blockPos : BlockPos.iterate(pos1, pos2)) {
            BlockPos addPos = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            Block block = WorldHelper.INSTANCE.getBlock(blockPos);
            if (block instanceof CropBlock) {
                CropBlock cropBlock = (CropBlock)block;
                int age = (Integer)Wrapper.INSTANCE.getWorld().getBlockState(blockPos).get(cropBlock.getAgeProperty());
                if (age == cropBlock.getMaxAge()) {
                        cropsToBreak.add(addPos);
                }
            } else if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
                cropsToBreak.add(addPos);
            } else if (block == Blocks.SUGAR_CANE || block == Blocks.BAMBOO) {
                Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.down());
                if (belowBlock == block)
                    cropsToBreak.add(addPos);
            } else if (block == Blocks.AIR) {
                Block belowBlock = WorldHelper.INSTANCE.getBlock(blockPos.down());
                if (belowBlock == Blocks.FARMLAND)
                    cropsToReplant.add(addPos);
            }
        }
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity)entity;
                ItemStack itemStack = itemEntity.getStack();
                if (itemStack.getItem() == Items.WHEAT || itemStack.getItem() == Items.WHEAT_SEEDS || itemStack.getItem() == Items.POTATO || itemStack.getItem() == Items.CARROT || itemStack.getItem() == Items.MELON || itemStack.getItem() == Items.PUMPKIN || itemStack.getItem() == Items.SUGAR_CANE || itemStack.getItem() == Items.BAMBOO)
                    itemsToGrab.add(itemEntity);
            }
        }
    }
}
