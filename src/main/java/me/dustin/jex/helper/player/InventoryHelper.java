package me.dustin.jex.helper.player;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;
import java.util.Map;

public enum InventoryHelper {
    INSTANCE;

    public Inventory getInventory() {
        return Wrapper.INSTANCE.getLocalPlayer().getInventory();
    }

    public Inventory getInventory(Player playerEntity) {
        return playerEntity.getInventory();
    }

    public boolean isHotbarFull() {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = getInventory().getItem(i);
            if (itemStack == null || itemStack.getItem() instanceof AirItem)
                return false;
        }
        return true;
    }

    public boolean isInventoryFull() {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = getInventory().getItem(i);
            if (itemStack == null || itemStack.getItem() instanceof AirItem)
                return false;
        }
        return true;
    }

    public boolean isInventoryFullIgnoreHotbar() {
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = getInventory().getItem(i);
            if (itemStack == null || itemStack.getItem() instanceof AirItem)
                return false;
        }
        return true;
    }

    public int get(Item item) {
        for (int i = 0; i < 45; i++) {
            if (getInventory().getItem(i) != null && getInventory().getItem(i).getItem() == item)
                return i;
        }
        return -1;
    }

    public int getFromHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (getInventory().getItem(i) != null && getInventory().getItem(i).getItem() == item)
                return i;
        }
        return -1;
    }

    public int getEmptyHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (getInventory().getItem(i) == null || getInventory().getItem(i).getItem() instanceof AirItem)
                return i;
        }
        return -1;
    }


    public int getFromInv(Item item) {
        for (int i = 0; i < 36; i++) {
            if (getInventory().getItem(i) != null && getInventory().getItem(i).getItem() == item)
                return i;
        }
        return -1;
    }

    public boolean isInventoryFull(ItemStack stack) {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = getInventory().getItem(i);
            if (itemStack == null || itemStack.getItem() instanceof AirItem)
                return false;
        }
        int slot = getInventory().findSlotMatchingItem(stack);
        if (slot != -1) {
            if (getInventory().getItem(slot).getCount() < getInventory().getItem(slot).getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    public void windowClick(AbstractContainerMenu container, int slot, ClickType action) {
        windowClick(container, slot, action, 0);
    }

    public void windowClick(AbstractContainerMenu container, int slot, ClickType action, int clickData) {
        Wrapper.INSTANCE.getMultiPlayerGameMode().handleInventoryMouseClick(container.containerId, slot, clickData, action, Wrapper.INSTANCE.getLocalPlayer());
    }

    public void swapToHotbar(int slot, int hotbarSlot) {
        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, slot, ClickType.SWAP, hotbarSlot);
    }

    public void moveToOffhand(int slot) {
        boolean hasOffhand = Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() != Items.AIR;
        windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, slot < 9 ? slot + 36 : slot, ClickType.PICKUP);
        windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 45, ClickType.PICKUP);
        if (hasOffhand)
            windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, slot < 9 ? slot + 36 : slot, ClickType.PICKUP);
    }

    public void setSlot(int slot, boolean actual, boolean packet) {
        if (slot == getInventory().selected)
            return;
        if (actual) {
            getInventory().selected = slot;
        }
        if (packet) {
            NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCarriedItemPacket(slot));
            lastSlotSent = slot;
        }
    }

    public HashMap<Integer, ItemStack> getStacksFromShulker(ItemStack shulkerBox) {
        HashMap<Integer, ItemStack> stacks = Maps.newHashMap();
        CompoundTag nbttagcompound = shulkerBox.getTag();
        if (nbttagcompound == null) return stacks;


        CompoundTag nbttagcompound1 = nbttagcompound.getCompound("BlockEntityTag");
        for (int i = 0; i < nbttagcompound1.getList("Items", 10).size(); i++) {
            CompoundTag compound = nbttagcompound1.getList("Items", 10).getCompound(i);
            int slot = compound.getInt("Slot");
            ItemStack itemStack = ItemStack.of(compound);
            stacks.put(slot, itemStack);
        }
        return stacks;
    }

    public int getDepthStriderLevel() {
        ItemStack boots = getInventory().getArmor(0);
        if (boots.isEnchanted()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.getEnchantments(boots);
            if (equippedEnchants.containsKey(Enchantments.DEPTH_STRIDER)) {
                return equippedEnchants.get(Enchantments.DEPTH_STRIDER);
            }
        }
        return 0;
    }

    public boolean isContainerEmpty(AbstractContainerMenu container) {
        int most = Wrapper.INSTANCE.getLocalPlayer().containerMenu.slots.size() - 36;
        for (int i = 0; i < most; i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (stack != null && stack.getItem() != Items.AIR) {
                return false;
            }
        }
        return true;
    }

    public boolean hasEnchantment(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack.isEnchanted()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.getEnchantments(itemStack);
            if (equippedEnchants.containsKey(enchantment)) {
                return true;
            }
        }
        return false;
    }

    public boolean compareEnchants(ItemStack equippedStack, ItemStack newPiece, Enchantment enchantment) {
        int equippedLevel = 0;
        int newLevel = 0;
        if (equippedStack.isEnchanted()) {
            Map<Enchantment, Integer> equippedEnchants = EnchantmentHelper.getEnchantments(equippedStack);
            if (!equippedEnchants.isEmpty()) {
                if (equippedEnchants.containsKey(enchantment))
                    equippedLevel = equippedEnchants.get(enchantment);
            }
        }
        if (newPiece.isEnchanted()) {
            Map<Enchantment, Integer> newPieceEnchants = EnchantmentHelper.getEnchantments(newPiece);
            if (!newPieceEnchants.isEmpty()) {
                if (newPieceEnchants.containsKey(enchantment))
                    newLevel = newPieceEnchants.get(enchantment);
            }
        }
        return newLevel > equippedLevel;
    }

    public boolean isShulker(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() == Items.SHULKER_BOX || stack.getItem() == Items.BLACK_SHULKER_BOX || stack.getItem() == Items.BLUE_SHULKER_BOX || stack.getItem() == Items.BROWN_SHULKER_BOX || stack.getItem() == Items.CYAN_SHULKER_BOX || stack.getItem() == Items.GRAY_SHULKER_BOX || stack.getItem() == Items.GREEN_SHULKER_BOX || stack.getItem() == Items.LIGHT_BLUE_SHULKER_BOX || stack.getItem() == Items.LIGHT_GRAY_SHULKER_BOX || stack.getItem() == Items.LIME_SHULKER_BOX || stack.getItem() == Items.MAGENTA_SHULKER_BOX || stack.getItem() == Items.ORANGE_SHULKER_BOX || stack.getItem() == Items.PINK_SHULKER_BOX || stack.getItem() == Items.PURPLE_SHULKER_BOX || stack.getItem() == Items.RED_SHULKER_BOX || stack.getItem() == Items.WHITE_SHULKER_BOX || stack.getItem() == Items.YELLOW_SHULKER_BOX);
    }

    public int countItems(Item item) {
        int count = 0;
        for (int i = 0; i < 44; i++) {
            ItemStack itemStack = getInventory().getItem(i);
            if (itemStack != null && itemStack.getItem() == item)
                count+=itemStack.getCount();
        }
        return count;
    }

    public HashMap<Integer, ItemStack> getStacksFromInventory(boolean hotbar) {
        HashMap<Integer, ItemStack> stacks = Maps.newHashMap();
        if (hotbar) {
            for (int i = 0; i < 9; i++) {
                stacks.put(i + 36, getInventory().getItem(i));
            }
        }
        for (int i = 9; i < 44; i++) {
            stacks.put(i - 9, getInventory().getItem(i));
        }
        return stacks;
    }

    public float getBlockBreakingSpeed(BlockState block, int slot) {
        Player player = Wrapper.INSTANCE.getLocalPlayer();
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

    private int lastSlotSent = -1;

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        ServerboundSetCarriedItemPacket updateSelectedSlotC2SPacket = (ServerboundSetCarriedItemPacket) event.getPacket();
        if (updateSelectedSlotC2SPacket.getSlot() == lastSlotSent)
            event.cancel();
        lastSlotSent = updateSelectedSlotC2SPacket.getSlot();
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundSetCarriedItemPacket.class));

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        lastSlotSent = -1;
    });
}