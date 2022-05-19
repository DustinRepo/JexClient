package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SpawnProofing extends Feature {

    @Op(name = "Delay", max = 1000, inc = 10)
    public int delay = 50;
    @Op(name = "Range", max = 6, min = 2)
    public int range = 5;
    @Op(name = "Use Glass")
    public boolean useGlass = true;

    private final StopWatch stopWatch = new StopWatch();

    public SpawnProofing() {
        super(Category.WORLD, "Automatically place carpets/slabs to spawn proof around you");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delay) || KillAura.INSTANCE.hasTarget() || AutoEat.isEating)
            return;
        stopWatch.reset();
        for (int x = -range; x < range; x++) {
            for (int y = -range; y < range; y++) {
                for (int z = -range; z < range; z++) {
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    BlockState blockState = WorldHelper.INSTANCE.getBlockState(pos);
                    BlockState belowState = WorldHelper.INSTANCE.getBlockState(pos.down());
                    if (blockState.getFluidState().isEmpty() && blockState.getMaterial().isReplaceable() && !WorldHelper.INSTANCE.canUseOnPos(pos.down()) && (belowState.hasSolidTopSurface(Wrapper.INSTANCE.getWorld(), pos.down(), Wrapper.INSTANCE.getLocalPlayer()) || belowState.getBlock() instanceof SoulSandBlock) && !(belowState.getBlock() instanceof GlassBlock || belowState.getBlock() instanceof StainedGlassBlock || belowState.getBlock() == Blocks.SEA_LANTERN || belowState.getBlock() == Blocks.GLOWSTONE || belowState.getBlock() == Blocks.BEDROCK)) {
                        int spawnproofItem = getSpawnProofingItem();
                        if (spawnproofItem == -1)
                            return;
                        if (spawnproofItem > 8) {
                            InventoryHelper.INSTANCE.swapToHotbar(spawnproofItem, 8);
                            spawnproofItem = 8;
                        }
                        InventoryHelper.INSTANCE.setSlot(spawnproofItem, true, true);
                        BlockHitResult blockHitResult = new BlockHitResult(Vec3d.ofBottomCenter(pos), Direction.UP, pos.down(), false);
                        Wrapper.INSTANCE.getMultiPlayerGameMode().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, blockHitResult);
                        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);

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
            if (itemStack.getItem() instanceof BlockItem blockItem && (blockItem.getBlock() instanceof AbstractButtonBlock || blockItem.getBlock() instanceof CarpetBlock || blockItem.getBlock() instanceof PressurePlateBlock || blockItem.getBlock() instanceof SlabBlock || blockItem.getBlock() == Blocks.SEA_LANTERN || blockItem.getBlock() == Blocks.GLOWSTONE || ((blockItem.getBlock() instanceof GlassBlock || blockItem.getBlock() instanceof StainedGlassBlock) && useGlass))) {
                return i;
            }
        }
        return -1;
    }

}
