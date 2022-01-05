package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Automatically place carpets/slabs to spawn proof around you")
public class SpawnProofing extends Feature {

    @Op(name = "Delay", max = 1000, inc = 10)
    public int delay = 50;
    @Op(name = "Range", max = 6, min = 2)
    public int range = 5;
    @Op(name = "Use Glass")
    public boolean useGlass = true;

    private final Timer timer = new Timer();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!timer.hasPassed(delay) || KillAura.INSTANCE.hasTarget() || AutoEat.isEating)
            return;
        timer.reset();
        for (int x = -range; x < range; x++) {
            for (int y = -range; y < range; y++) {
                for (int z = -range; z < range; z++) {
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    BlockState blockState = WorldHelper.INSTANCE.getBlockState(pos);
                    BlockState belowState = WorldHelper.INSTANCE.getBlockState(pos.down());
                    if (blockState.getFluidState().isEmpty() && blockState.getMaterial().isReplaceable() && (belowState.hasSolidTopSurface(Wrapper.INSTANCE.getWorld(), pos.down(), Wrapper.INSTANCE.getLocalPlayer()) || belowState.getBlock() instanceof SoulSandBlock) && !(belowState.getBlock() instanceof GlassBlock || belowState.getBlock() instanceof StainedGlassBlock || belowState.getBlock() == Blocks.BEDROCK)) {
                        int spawnproofItem = getSpawnProofingItem();
                        if (spawnproofItem == -1)
                            return;
                        if (spawnproofItem > 8) {
                            InventoryHelper.INSTANCE.swapToHotbar(spawnproofItem, 8);
                            spawnproofItem = 8;
                        }
                        InventoryHelper.INSTANCE.setSlot(spawnproofItem, true, true);
                        PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, false);

                        ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(spawnproofItem);
                        setSuffix(itemStack.getName().getString());
                        if (delay != 0)
                            return;
                    }
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private int getSpawnProofingItem() {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (itemStack.getItem() instanceof BlockItem blockItem && (blockItem.getBlock() instanceof CarpetBlock || blockItem.getBlock() instanceof PressurePlateBlock || blockItem.getBlock() instanceof SlabBlock || ((blockItem.getBlock() instanceof GlassBlock || blockItem.getBlock() instanceof StainedGlassBlock) && useGlass))) {
                return i;
            }
        }
        return -1;
    }

}
