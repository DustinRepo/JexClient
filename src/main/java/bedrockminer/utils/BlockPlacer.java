package bedrockminer.utils;

import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlacer {
    public static void simpleBlockPlacement(BlockPos pos, ItemLike item) {
        Minecraft minecraftClient = Minecraft.getInstance();

        InventoryManager.switchToItem(item);
        BlockHitResult hitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false);
//        minecraftClient.interactionManager.interactBlock(minecraftClient.player, minecraftClient.world, Hand.MAIN_HAND, hitResult);
        placeBlockWithoutInteractingBlock(minecraftClient, hitResult);
    }

    public static void pistonPlacement(BlockPos pos, Direction direction) {
        Minecraft minecraftClient = Minecraft.getInstance();
        double x = pos.getX();

        switch (BreakingFlowController.getWorkingMode()) {
            case CARPET_EXTRA://carpet accurateBlockPlacement支持
                x = x + 2 + direction.get3DDataValue() * 2;
                break;
            case VANILLA://直接发包，改变服务端玩家实体视角
                Player player = minecraftClient.player;
                float pitch;
                switch (direction) {
                    case UP:
                        pitch = 90f;
                        break;
                    case DOWN:
                        pitch = -90f;
                        break;
                    default:
                        pitch = 90f;
                        break;
                }

                minecraftClient.getConnection().send(new ServerboundMovePlayerPacket.Rot(player.getViewYRot(1.0f), pitch, player.isOnGround()));
                break;
        }

        Vec3 vec3d = new Vec3(x, pos.getY(), pos.getZ());

        InventoryManager.switchToItem(Blocks.PISTON);
        BlockHitResult hitResult = new BlockHitResult(vec3d, Direction.UP, pos, false);
//        minecraftClient.interactionManager.interactBlock(minecraftClient.player, minecraftClient.world, Hand.MAIN_HAND, hitResult);
        placeBlockWithoutInteractingBlock(minecraftClient, hitResult);
    }

    private static void placeBlockWithoutInteractingBlock(Minecraft minecraftClient, BlockHitResult hitResult) {
        LocalPlayer player = minecraftClient.player;
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, hitResult);

        if (!itemStack.isEmpty() && !player.getCooldowns().isOnCooldown(itemStack.getItem())) {
            UseOnContext itemUsageContext = new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult);
            itemStack.useOn(itemUsageContext);

        }
    }
}
