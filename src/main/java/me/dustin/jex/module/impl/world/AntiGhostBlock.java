package me.dustin.jex.module.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.Hand;

@ModClass(name = "AntiGhostBlock", category = ModCategory.WORLD, description = "Prevent the game from creating ghost blocks.")
public class AntiGhostBlock extends Module {

    @EventListener(events = {EventBreakBlock.class})
    public void breakB(EventBreakBlock eventBreakBlock) {
        Block block = WorldHelper.INSTANCE.getBlock(eventBreakBlock.getPos());
        ItemStack stack = PlayerHelper.INSTANCE.mainHandStack();
        if (stack != null && stack.getItem() instanceof ToolItem) {
            PlayerHelper.INSTANCE.rightClickBlock(eventBreakBlock.getPos(), Hand.MAIN_HAND, true);
        }
    }

}
