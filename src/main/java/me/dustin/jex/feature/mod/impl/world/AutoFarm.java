package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.KeyPressFilter;
import me.dustin.jex.event.filters.MousePressFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.PathingHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Mine out a selected area")
public class AutoFarm extends Feature {

    @Op(name = "Render Area Box")
    public boolean renderAreaBox = true;
    @Op(name = "Sort Delay", max = 1000, inc = 10)
    public int sortDelay = 350;

    public static FarmingArea farmArea;
    private Stage stage = Stage.SET_POS1;

    private BlockPos tempPos1;
    private BlockPos tempPos2;
    private final StopWatch sortStopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating)
            return;
        if (farmArea == null)
            return;
        if (stage == Stage.PAUSED)
            return;
        if (BonemealAura.INSTANCE.isBonemealing())
            return;
        BlockPos closest;
        if (KillAura.INSTANCE.hasTarget())
            return;
        switch (stage) {
            case FARMING -> {
                closest = farmArea.getClosestCrop();
                //if no plants to farm move to next stage
                if (closest == null) {
                    this.stage = Stage.PLANTING;
                    PathingHelper.INSTANCE.cancelPathing();
                    return;
                }
                if (!PathingHelper.INSTANCE.isPathing()) {
                    PathingHelper.INSTANCE.setAllowMining(false);
                    PathingHelper.INSTANCE.pathTo(closest);
                }
                double distanceTo = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), Vec3.atCenterOf(closest));

                if (distanceTo <= 3) {
                    breakBlock(closest, event);
                }
            }
            case PLANTING -> {
                closest = farmArea.getClosestFarmland();
                int cropSlot = getPlantableCrop();

                //if out of crops to plant or no more area to plant crops move to next stage
                if (cropSlot == -1 || closest == null) {
                    this.stage = Stage.ITEM_PICKUP;
                    PathingHelper.INSTANCE.cancelPathing();
                    return;
                }

                if (cropSlot > 8) {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, cropSlot, ClickType.SWAP, 8);
                    cropSlot = 8;
                }

                if (!PathingHelper.INSTANCE.isPathing()) {
                    PathingHelper.INSTANCE.setAllowMining(false);
                    PathingHelper.INSTANCE.pathTo(closest);
                }
                double distanceTo = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), Vec3.atCenterOf(closest));

                InventoryHelper.INSTANCE.setSlot(cropSlot, true, true);

                if (distanceTo <= 3) {
                    PlayerHelper.INSTANCE.placeBlockInPos(closest, InteractionHand.MAIN_HAND, false);
                    PathingHelper.INSTANCE.cancelPathing();
                }
            }
            case ITEM_PICKUP -> {
                if (getClosestItem() == null) {
                    this.stage = Stage.WAITING;
                    PathingHelper.INSTANCE.cancelPathing();
                    return;
                }

                closest = getClosestItem().blockPosition();
                if (!PathingHelper.INSTANCE.isPathing()) {
                    PathingHelper.INSTANCE.setAllowMining(false);
                    PathingHelper.INSTANCE.pathTo(closest, 10);
                }
            }
            case WAITING -> {
                //do farming check first
                closest = farmArea.getClosestCrop();
                if (closest != null) {
                    stage = Stage.FARMING;
                    return;
                }
                //do planting check
                closest = farmArea.getClosestFarmland();
                int cropSlot = getPlantableCrop();
                if (cropSlot != -1 && closest != null) {
                    this.stage = Stage.PLANTING;
                    return;
                }
                //do item check
                if (getClosestItem() != null) {
                    stage = Stage.ITEM_PICKUP;
                    return;
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {

        if (farmArea != null && renderAreaBox) {
            Vec3 miningAreaVec1 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(farmArea.getAreaBB().minX, farmArea.getAreaBB().minY, farmArea.getAreaBB().minZ));
            Vec3 miningAreaVec2 = Render3DHelper.INSTANCE.getRenderPosition(new BlockPos(farmArea.getAreaBB().maxX, farmArea.getAreaBB().maxY, farmArea.getAreaBB().maxZ));
            AABB miningAreaBox = new AABB(miningAreaVec1.x, miningAreaVec1.y, miningAreaVec1.z, miningAreaVec2.x + 1, miningAreaVec2.y + 1, miningAreaVec2.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), miningAreaBox, 0xffffff00);
        } else if (tempPos1 != null) {//draws yellow box on first set pos
            Vec3 tempVec = Render3DHelper.INSTANCE.getRenderPosition(tempPos1);
            AABB closestBox = new AABB(tempVec.x, tempVec.y, tempVec.z, tempVec.x + 1, tempVec.y + 1, tempVec.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), closestBox, 0xffffff00);
        }
        if (tempPos2 != null) {//draws yellow box on first set pos
            Vec3 tempVec = Render3DHelper.INSTANCE.getRenderPosition(tempPos2);
            AABB closestBox = new AABB(tempVec.x, tempVec.y, tempVec.z, tempVec.x + 1, tempVec.y + 1, tempVec.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), closestBox, 0xffffff00);
        }
        //draws yellow box on crosshair block
        if (farmArea == null && Wrapper.INSTANCE.getMinecraft().hitResult instanceof BlockHitResult blockHitResult) {
            Vec3 hitVec = Render3DHelper.INSTANCE.getRenderPosition(blockHitResult.getBlockPos());
            AABB hoverBox = new AABB(hitVec.x, hitVec.y, hitVec.z, hitVec.x + 1, hitVec.y + 1, hitVec.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), hoverBox, 0xff00ff00);
        }
    });

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getMinecraft().hitResult instanceof BlockHitResult blockHitResult) {
            switch (stage) {
                case SET_POS1 -> tempPos1 = blockHitResult.getBlockPos();
                case SET_POS2 -> tempPos2 = blockHitResult.getBlockPos();
            }
        }
    }, new MousePressFilter(EventMouseButton.ClickType.IN_GAME, 1));

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        String message = "";
        switch (stage) {
            case SET_POS1 -> {
                if (tempPos1 == null) {
                    message = "Select First Pos with Right-Click";
                } else {
                    message = "Press Enter to Confirm Pos 1";
                }
            }
            case SET_POS2 -> {
                if (tempPos2 == null) {
                    message = "Select Second Pos with Right-Click";
                } else {
                    message = "Press Enter to Confirm Pos 2";
                }
            }
            case PAUSED -> message = "AutoFarm Paused... Press Enter to Resume";
        }
        if (message.isEmpty())
            message = ChatFormatting.WHITE + "AutoFarm Stage: " + ChatFormatting.GREEN + StringUtils.capitalize(stage.name().toLowerCase().replace("_", " "));

        float width = FontHelper.INSTANCE.getStringWidth(message);
        Render2DHelper.INSTANCE.outlineAndFill(event.getPoseStack(), Render2DHelper.INSTANCE.getScaledWidth() / 2.f - width / 2.f - 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 10, Render2DHelper.INSTANCE.getScaledWidth() / 2.f + width / 2.f + 2, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 24, 0x70696969, 0x40000000);
        FontHelper.INSTANCE.drawCenteredString(event.getPoseStack(), message, Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f + 13, farmArea != null ? 0xffff0000 : -1);
    });

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() == GLFW.GLFW_KEY_ENTER) {
            switch (stage) {
                case SET_POS1 -> {
                    if (tempPos1 != null)
                        stage = Stage.SET_POS2;
                }
                case SET_POS2 -> {
                    if (tempPos2 != null)
                        if (farmArea == null) {
                            farmArea = new FarmingArea(tempPos1, tempPos2);
                            stage = Stage.FARMING;
                            tempPos1 = null;
                            tempPos2 = null;
                        }
                }
                case FARMING -> {
                    stage = Stage.PAUSED;
                    PathProcessor.releaseControls();
                }
                case PAUSED -> stage = Stage.FARMING;
            }
        } else if (event.getKey() == GLFW.GLFW_KEY_BACKSPACE) {
            switch (stage) {
                case SET_POS1 -> {
                    if (tempPos1 != null)
                        tempPos1 = null;
                }
                case SET_POS2 -> {
                    if (tempPos2 != null)
                        tempPos2 = null;
                    else
                        stage = Stage.SET_POS1;
                }
                case PAUSED -> stage = Stage.FARMING;
            }
        }
    }, new KeyPressFilter(EventKeyPressed.PressType.IN_GAME, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_BACKSPACE));

    private ItemEntity getClosestItem() {
        double distance = 9999;
        ItemEntity closest = null;
        for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
            if (entity instanceof ItemEntity itemEntity) {
                if (itemEntity.tickCount < 20)//let it be alive for 1 second before we go for it
                    continue;
                double distanceTo = ClientMathHelper.INSTANCE.getDistance(entity.position(), Wrapper.INSTANCE.getLocalPlayer().position());
                if (isPlantableCrop(itemEntity.getItem().getItem()) && (closest == null || distanceTo < distance))
                    closest = itemEntity;
            }
        }
        return closest;
    }

    private boolean isPlantableCrop(Item item) {
        if (InventoryHelper.INSTANCE.isInventoryFull(new ItemStack(item)))
            return false;
        return item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS || item == Items.POTATO || item == Items.CARROT;
    }

    private int getPlantableCrop() {
        int i = InventoryHelper.INSTANCE.get(Items.WHEAT_SEEDS);
        if (i != -1)
            return i;
        i = InventoryHelper.INSTANCE.get(Items.BEETROOT_SEEDS);
        if (i != -1)
            return i;
        i = InventoryHelper.INSTANCE.get(Items.POTATO);
        if (i != -1)
            return i;
        i = InventoryHelper.INSTANCE.get(Items.CARROT);
        return i;
    }

    public void breakBlock(BlockPos closestBlock, EventPlayerPackets event) {
        BlockHitResult blockHitResult = rayCast(closestBlock);
        BlockPos blockPos = closestBlock;
        if (blockHitResult != null) {
            if (ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), ClientMathHelper.INSTANCE.getVec(blockHitResult.getBlockPos())) < ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), ClientMathHelper.INSTANCE.getVec(closestBlock))) {
                blockPos = blockHitResult.getBlockPos();
            }
        }
        RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), Vec3.atCenterOf(blockPos));
        event.setRotation(rotationVector);
        Wrapper.INSTANCE.getLocalPlayer().setYHeadRot(rotationVector.getYaw());
        Wrapper.INSTANCE.getLocalPlayer().setYBodyRot(rotationVector.getYaw());

        Wrapper.INSTANCE.getMultiPlayerGameMode().continueDestroyBlock(blockPos, blockHitResult == null ? Direction.UP : blockHitResult.getDirection());
        Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
    }

    public boolean isPaused() {
        return this.stage != Stage.FARMING && stage != Stage.PLANTING && stage != Stage.ITEM_PICKUP;
    }

    @Override
    public void onEnable() {
        stage = Stage.SET_POS1;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        tempPos1 = null;
        tempPos2 = null;
        farmArea = null;
        PathingHelper.INSTANCE.cancelPathing();
        PathProcessor.releaseControls();
        super.onDisable();
    }

    public Color getColor(double power) {
        if (power > 1)
            power = 1;
        double H = power * 0.35;
        double S = 0.9;
        double B = 0.9;

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }

    public BlockHitResult rayCast(BlockPos blockPos) {
        RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), Vec3.atLowerCornerOf(blockPos).add(0.5, 0, 0.5));
        RotationVector saved = new RotationVector(Wrapper.INSTANCE.getLocalPlayer());
        PlayerHelper.INSTANCE.setRotation(rotationVector);
        HitResult result = Wrapper.INSTANCE.getLocalPlayer().pick(Wrapper.INSTANCE.getMultiPlayerGameMode().getPickRange(), 1, false);// Wrapper.clientWorld().rayTraceBlock(getVec(entity), getVec(entity).add(0, -256, 0), false, true, false);
        PlayerHelper.INSTANCE.setRotation(saved);
        if (result instanceof BlockHitResult blockHitResult)
            return blockHitResult;
        return null;
    }

    public static class FarmingArea {
        private final AutoFarm autoFarm;
        private final AABB areaBB;

        private final ArrayList<BlockPos> blockPosList = new ArrayList<>();

        public FarmingArea(BlockPos pos1, BlockPos pos2) {
            BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
            this.areaBB = new AABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            this.autoFarm = Feature.get(AutoFarm.class);

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        blockPosList.add(new BlockPos(x, y, z));
                    }
                }
            }
            sortList();
        }

        public BlockPos getClosestCrop() {
            if (autoFarm.sortStopWatch.hasPassed(autoFarm.sortDelay)) {
                sortList();
                autoFarm.sortStopWatch.reset();
            }
            for (BlockPos blockPos : blockPosList) {
                if (WorldHelper.INSTANCE.isCrop(blockPos, true)) {
                    return blockPos;
                }
            }
            return null;
        }

        public BlockPos getClosestFarmland() {
            if (autoFarm.sortStopWatch.hasPassed(autoFarm.sortDelay)) {
                sortList();
                autoFarm.sortStopWatch.reset();
            }
            for (BlockPos blockPos : blockPosList) {
                Block block = WorldHelper.INSTANCE.getBlock(blockPos);
                Block below = WorldHelper.INSTANCE.getBlock(blockPos.below());
                if (block == Blocks.AIR && below == Blocks.FARMLAND) {
                    return blockPos;
                }
            }
            return null;
        }

        public AABB getAreaBB() {
            return areaBB;
        }

        public void sortList() {
            blockPosList.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position().add(0, 2, 0), Vec3.atCenterOf(value))));
        }
    }

    private enum Stage {
        SET_POS1, SET_POS2, FARMING, PLANTING, ITEM_PICKUP, WAITING, PAUSED
    }
}
