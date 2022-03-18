package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Destroy any fully grown crops nearby")
public class FarmAura extends Feature {

    @Op(name = "Check Age")
    public boolean checkAge = true;
    @Op(name = "Break Delay (MS)", max = 1000, inc = 10)
    public int breakDelay = 100;
    @Op(name = "Plant Delay (MS)", max = 1000, inc = 10)
    public int plantDelay = 100;

    private final StopWatch breakStopWatch = new StopWatch();
    private final StopWatch plantStopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (BonemealAura.INSTANCE.isBonemealing())
            return;
        if (breakStopWatch.hasPassed(breakDelay)) {
            breakStopWatch.reset();
            BlockPos crop = getCrop();
            if (crop != null) {
                RotationVector rot = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(crop.getX(), crop.getY(), crop.getZ()));
                rot.normalize();
                event.setRotation(rot);
                Direction facing = Direction.fromRotation(-rot.getYaw());
                Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(crop, facing);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
            }
        }
        if (plantStopWatch.hasPassed(plantDelay)) {
            plantStopWatch.reset();
            BlockPos farmland = getFarmland();
            if (farmland != null) {
                int cropSlot = getPlantableCrop();
                if (cropSlot == -1)
                    return;
                if (cropSlot > 8) {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, cropSlot, SlotActionType.SWAP, 8);
                    cropSlot = 8;
                }
                InventoryHelper.INSTANCE.setSlot(cropSlot, true, true);

                RotationVector rot = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), Vec3d.ofCenter(farmland));
                rot.normalize();
                event.setRotation(rot);
                PlayerHelper.INSTANCE.placeBlockInPos(getFarmland().up(), Hand.MAIN_HAND, false);
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    if (WorldHelper.INSTANCE.isCrop(blockPos, checkAge)) {
                        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
                        Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBoxOutline(event.getMatrixStack(), box, 0xffff0000);
                    } else if (WorldHelper.INSTANCE.getBlock(blockPos.down()) == Blocks.FARMLAND && WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.AIR) {
                        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos.down());
                        Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBoxOutline(event.getMatrixStack(), box, 0xff00ff00);
                    }
                }
            }
        }
    });

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

    public BlockPos getFarmland() {
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z).down();
                    if (WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.FARMLAND && WorldHelper.INSTANCE.getBlock(blockPos.up()) == Blocks.AIR)
                        return blockPos;
                }
            }
        }
        return null;
    }

    public BlockPos getCrop() {
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    if (WorldHelper.INSTANCE.isCrop(blockPos, checkAge))
                        return blockPos;
                }
            }
        }
        return null;
    }
}
