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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

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
                RotationVector rot = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3(crop.getX(), crop.getY(), crop.getZ()));
                rot.normalize();
                event.setRotation(rot);
                Direction facing = Direction.fromYRot(-rot.getYaw());
                Wrapper.INSTANCE.getMultiPlayerGameMode().continueDestroyBlock(crop, facing);
                Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
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
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, cropSlot, ClickType.SWAP, 8);
                    cropSlot = 8;
                }
                InventoryHelper.INSTANCE.setSlot(cropSlot, true, true);

                RotationVector rot = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), Vec3.atCenterOf(farmland));
                rot.normalize();
                event.setRotation(rot);
                PlayerHelper.INSTANCE.placeBlockInPos(getFarmland().above(), InteractionHand.MAIN_HAND, false);
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().blockPosition().offset(x, y, z);
                    if (WorldHelper.INSTANCE.isCrop(blockPos, checkAge)) {
                        Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
                        AABB box = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBoxOutline(event.getPoseStack(), box, 0xffff0000);
                    } else if (WorldHelper.INSTANCE.getBlock(blockPos.below()) == Blocks.FARMLAND && WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.AIR) {
                        Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos.below());
                        AABB box = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBoxOutline(event.getPoseStack(), box, 0xff00ff00);
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
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().blockPosition().offset(x, y, z).below();
                    if (WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.FARMLAND && WorldHelper.INSTANCE.getBlock(blockPos.above()) == Blocks.AIR)
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
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().blockPosition().offset(x, y, z);
                    if (WorldHelper.INSTANCE.isCrop(blockPos, checkAge))
                        return blockPos;
                }
            }
        }
        return null;
    }
}
