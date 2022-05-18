package me.dustin.jex.feature.mod.impl.world;

import bedrockminer.utils.BreakingFlowController;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.event.world.EventPlayerInteractionTick;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Break bedrock in survival mode using a well-known bug. Code from https://github.com/aria1th/Fabric-Bedrock-Miner")
public class BedrockBreaker extends Feature {

    @EventPointer
    private final EventListener<EventPlayerInteractionTick> eventPlayerInteractionTickEventListener = new EventListener<>(event -> {
        BreakingFlowController.tick();
    });

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        BlockPos blockPos = event.getBlockPos();
        Block block = WorldHelper.INSTANCE.getBlock(blockPos);
        if (block == Blocks.BEDROCK) {
            BreakingFlowController.addBlockPosToList(blockPos);
        }
    }, new ClickBlockFilter(EventClickBlock.Mode.PRE));

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            ChatHelper.INSTANCE.addClientMessage("Don't know how to use? Have these items ready:");
            ChatHelper.INSTANCE.addClientMessage("1. Efficiency V diamond (or netherite) pickaxe");
            ChatHelper.INSTANCE.addClientMessage("2. Haste II beacon");
            ChatHelper.INSTANCE.addClientMessage("3. Pistons");
            ChatHelper.INSTANCE.addClientMessage("4. Redstone torches");
            ChatHelper.INSTANCE.addClientMessage("5. Slime blocks\n");
            ChatHelper.INSTANCE.addClientMessage("Then simply left-click Bedrock with no blocks directly above it.");
        }
        super.onEnable();
    }
}
