package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;

public class FarmAura extends Feature {

    public final Property<Boolean> checkAgeProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Check Age")
            .value(true)
            .build();
    public final Property<Long> breakDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Break Delay (MS)")
            .value(100L)
            .max(1000)
            .inc(10)
            .build();
    public final Property<Long> plantDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Plant Delay (MS)")
            .value(100L)
            .max(1000)
            .inc(10)
            .build();

    private final StopWatch breakStopWatch = new StopWatch();
    private final StopWatch plantStopWatch = new StopWatch();

    public FarmAura() {
        super(Category.WORLD, "Destroy any fully grown crops nearby");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (BonemealAura.INSTANCE.isBonemealing())
            return;
        if (breakStopWatch.hasPassed(breakDelayProperty.value())) {
            breakStopWatch.reset();
            BlockPos crop = getCrop();
            if (crop != null) {
                RotationVector rot = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(crop.getX(), crop.getY(), crop.getZ()));
                rot.normalize();
                event.setRotation(rot);
                Direction facing = Direction.fromRotation(-rot.getYaw());
                Wrapper.INSTANCE.getClientPlayerInteractionManager().updateBlockBreakingProgress(crop, facing);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
            }
        }
        if (plantStopWatch.hasPassed(plantDelayProperty.value())) {
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
                    if (WorldHelper.INSTANCE.isCrop(blockPos, checkAgeProperty.value())) {
                        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos);
                        Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                        Render3DHelper.INSTANCE.drawBoxOutline(event.getPoseStack(), box, 0xffff0000);
                    } else if (WorldHelper.INSTANCE.getBlock(blockPos.down()) == Blocks.FARMLAND && WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.AIR) {
                        Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockPos.down());
                        Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
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
                    if (WorldHelper.INSTANCE.isCrop(blockPos, checkAgeProperty.value()))
                        return blockPos;
                }
            }
        }
        return null;
    }
}
