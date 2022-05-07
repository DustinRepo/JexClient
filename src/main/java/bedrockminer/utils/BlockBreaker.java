package bedrockminer.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;

//import java.util.ArrayList;

//import static net.minecraft.block.Block.sideCoversSmallSquare;

public class BlockBreaker {
    public static void breakBlock(ClientLevel world, BlockPos pos) {
        InventoryManager.switchToItem(Items.DIAMOND_PICKAXE);
        Minecraft.getInstance().gameMode.startDestroyBlock(pos, Direction.UP);
    }


}