package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Replaces mycelium with grass blocks")
public class LawnBot extends Feature {

    private final StopWatch stopWatch = new StopWatch();

    private final ArrayList<BlockPos> myceliumSpots = new ArrayList<>();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Item grassBlockItem = Items.GRASS_BLOCK;
        int grassCount = InventoryHelper.INSTANCE.countItems(grassBlockItem);
        if (grassCount == 0) {
            return;
        }
        int grassHotbarSlot = InventoryHelper.INSTANCE.getFromHotbar(grassBlockItem);
        if (grassHotbarSlot == -1) {
            int grassInvSlot = InventoryHelper.INSTANCE.getFromInv(grassBlockItem);
            if (grassInvSlot == -1)
                return;
            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, grassInvSlot < 9 ? grassInvSlot + 36 : grassInvSlot, SlotActionType.SWAP, 8);
            return;
        }
        for (int i = 0; i < myceliumSpots.size(); i++) {
            BlockPos pos = myceliumSpots.get(i);
            Block block = WorldHelper.INSTANCE.getBlock(pos);
            float distance = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), ClientMathHelper.INSTANCE.getVec(pos));
            if (block == Blocks.AIR && distance <= 5) {
                InventoryHelper.INSTANCE.setSlot(grassHotbarSlot, true, true);
                PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, false);
                return;
            } else if (block != Blocks.MYCELIUM) {
                myceliumSpots.remove(i);
            }
        }
        for (int i = 0; i < myceliumSpots.size(); i++) {
            BlockPos pos = myceliumSpots.get(i);
            Block block = WorldHelper.INSTANCE.getBlock(pos);
            float distance = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), ClientMathHelper.INSTANCE.getVec(pos));
            if (block == Blocks.MYCELIUM && distance <= 5) {
                Wrapper.INSTANCE.getMultiPlayerGameMode().updateBlockBreakingProgress(pos, Direction.UP);
                return;
            }
        }
        updateMyceliumSpots();
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private void updateMyceliumSpots() {
        if (stopWatch.hasPassed(250)) {
            myceliumSpots.clear();
            for (int x = -5; x < 5; x++) {
                for (int y = -3; y < 3; y++) {
                    for (int z = -5; z < 5; z++) {
                        BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                        if (WorldHelper.INSTANCE.getBlock(pos) == Blocks.MYCELIUM) {
                            myceliumSpots.add(pos);
                        }
                    }
                }
            }
            stopWatch.reset();
        }
    }
}
