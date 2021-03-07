package me.dustin.jex.module.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.world.EventBreakBlock;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;

@ModClass(name = "AntiGhostBlock", category = ModCategory.WORLD, description = "Prevent the game from creating ghost blocks.")
public class AntiGhostBlock extends Module {

    @EventListener(events = {EventBreakBlock.class})
    public void breakB(EventBreakBlock eventBreakBlock) {
        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, eventBreakBlock.getPos(), Direction.UP));
    }

}
