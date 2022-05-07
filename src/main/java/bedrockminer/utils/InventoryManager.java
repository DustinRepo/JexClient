package bedrockminer.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class InventoryManager {
    public static boolean switchToItem(ItemLike item) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Inventory playerInventory = minecraftClient.player.getInventory();

        int i = playerInventory.findSlotMatchingItem(new ItemStack(item));

        if ("diamond_pickaxe".equals(item.toString())) {
            i = getEfficientTool(playerInventory);
        }

        if (i != -1) {
            if (Inventory.isHotbarSlot(i)) {
                playerInventory.selected = i;
            } else {
                minecraftClient.gameMode.handlePickItem(i);
            }
            minecraftClient.getConnection().send(new ServerboundSetCarriedItemPacket(playerInventory.selected));
            return true;
        }
        return false;
    }

    private static int getEfficientTool(Inventory playerInventory) {
        for (int i = 0; i < playerInventory.items.size(); ++i) {
            if (getBlockBreakingSpeed(Blocks.PISTON.defaultBlockState(), i) > 45f) {
                return i;
            }
        }
        return -1;
    }

    public static boolean canInstantlyMinePiston() {
        Minecraft minecraftClient = Minecraft.getInstance();
        Inventory playerInventory = minecraftClient.player.getInventory();

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            if (getBlockBreakingSpeed(Blocks.PISTON.defaultBlockState(), i) > 45f) {
                return true;
            }
        }
        return false;
    }

    private static float getBlockBreakingSpeed(BlockState block, int slot) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Player player = minecraftClient.player;
        ItemStack stack = player.getInventory().getItem(slot);

        float f = stack.getDestroySpeed(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (float) (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float k;
            switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0:
                    k = 0.3F;
                    break;
                case 1:
                    k = 0.09F;
                    break;
                case 2:
                    k = 0.0027F;
                    break;
                case 3:
                default:
                    k = 8.1E-4F;
            }

            f *= k;
        }

        if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.isOnGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public static int getInventoryItemCount(ItemLike item) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Inventory playerInventory = minecraftClient.player.getInventory();
        return playerInventory.countItem((Item) item);
    }

    public static String warningMessage() {
        Minecraft minecraftClient = Minecraft.getInstance();
        if (minecraftClient.gameMode == null)
            return "null interaction manager";
        if (minecraftClient.player == null)
            return "null player";
        if (minecraftClient.level == null)
            return "null world";
        if (!"survival".equals(minecraftClient.gameMode.getPlayerMode().getName())) {
            return "仅限生存模式！Survival Only!";
        }

        if (InventoryManager.getInventoryItemCount(Items.PISTON) < 2) {
            return "活塞不够啦！ Needs more piston!";
        }

        if (InventoryManager.getInventoryItemCount(Items.REDSTONE_TORCH) < 1) {
            return "红石火把不够啦！ Needs more redstone torch!";
        }

        if (InventoryManager.getInventoryItemCount(Items.SLIME_BLOCK)<1){
            return "黏液块不够啦！ Needs more slime block!";
        }

        if (!InventoryManager.canInstantlyMinePiston()) {
            return "无法秒破活塞！请确保效率Ⅴ+急迫Ⅱ Can't instantly mine piston! EfficiencyⅤ+HasteⅡ required!";
        }
        return null;
    }

}
