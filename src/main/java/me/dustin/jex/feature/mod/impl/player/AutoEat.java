package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerUpdates;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Eat food when hunger is low.")
public class AutoEat extends Feature {

    public static boolean isEating;
    @Op(name = "Mode", all = {"Saturation", "Hunger"})
    public String mode = "Saturation";
    @Op(name = "Press Key")
    public boolean pressKey;
    @Op(name = "Eat Negative Foods")
    public boolean negativeFoods;
    @Op(name = "Eat To Regen")
    public boolean eatToRegen;
    private boolean wasEating;
    private int savedSlot = 0;
    private int lastFood;

    private final StopWatch baritoneStopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerUpdates> eventPlayerUpdatesEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerUpdates.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer() != null && getBestFood().itemStack != null) {
                setSuffix(getBestFood().itemStack.getHoverName().getString());
            } else {
                setSuffix("None");
            }
            if (!isEating && wasEating) {
                if (BaritoneHelper.INSTANCE.baritoneExists())
                    BaritoneHelper.INSTANCE.resume();
            }
            if (Wrapper.INSTANCE.getLocalPlayer() == null || (Feature.getState(Freecam.class) && Feature.get(Freecam.class).stealth) || Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
                isEating = false;
                if (BaritoneHelper.INSTANCE.baritoneExists()) {
                    BaritoneHelper.INSTANCE.resume();
                    baritoneStopWatch.reset();
                }
                return;
            }
            if (getBestFood().slot != -1 && getBestFood().itemStack != null && needsToEat(getBestFood())) {
                if (EntityHelper.INSTANCE.isAuraBlocking())
                    PlayerHelper.INSTANCE.unblock();
                if (!isEating) {
                    savedSlot = InventoryHelper.INSTANCE.getInventory().selected;
                    if (BaritoneHelper.INSTANCE.baritoneExists())
                        BaritoneHelper.INSTANCE.pause();
                    InventoryHelper.INSTANCE.setSlot(getBestFood().slot, true, true);
                    lastFood = Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel();
                    isEating = true;
                }
                if (lastFood != Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel()) {
                    if (lastFood < Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel()) {
                        isEating = false;
                        if (pressKey)
                            Wrapper.INSTANCE.getOptions().keyUse.setDown(false);
                        NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.UP));
                        InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                    }
                    lastFood = Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel();
                    if (BaritoneHelper.INSTANCE.baritoneExists()) {
                        BaritoneHelper.INSTANCE.resume();
                    }
                }
                if (isEating) {
                    if (pressKey)
                        Wrapper.INSTANCE.getOptions().keyUse.setDown(true);
                    Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getPlayer(), InteractionHand.MAIN_HAND);
                }
            } else if (isEating) {
                isEating = false;
                NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.UP));
                InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                if (BaritoneHelper.INSTANCE.baritoneExists())
                    BaritoneHelper.INSTANCE.resume();
            }
            wasEating = isEating;
        }
    });

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (event.getPacket() instanceof ServerboundSetCarriedItemPacket updateSelectedSlotC2SPacket && isEating) {
            if (updateSelectedSlotC2SPacket.getSlot() != getBestFood().slot)
                event.cancel();
        }
        if (event.getPacket() instanceof ServerboundPlayerActionPacket playerActionC2SPacket) {
            if (playerActionC2SPacket.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM && isEating)
                event.cancel();
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundSetCarriedItemPacket.class, ServerboundPlayerActionPacket.class));

    private boolean needsToEat(FoodInfo foodInfo) {
        if (!eatToRegen) {
            return 20 - Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel() >= foodInfo.item.getFoodProperties().getNutrition();
        } else {
            return 20 - Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel() >= foodInfo.item.getFoodProperties().getNutrition() || (Wrapper.INSTANCE.getLocalPlayer().getHealth() < 20 && Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel() < 18);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (isEating) {
            NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.UP));
        }
        isEating = false;
        if (BaritoneHelper.INSTANCE.baritoneExists())
            BaritoneHelper.INSTANCE.resume();
    }

    public FoodInfo getBestFood() {
        float points = 0;
        int slot = -1;
        ItemStack stack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getItem(i);
            if (itemStack != null && itemStack.getItem().isEdible()) {
                Item item = itemStack.getItem();
                switch (mode) {
                    case "Saturation":
                        if (isValidFood(item.getFoodProperties()) && item.getFoodProperties().getSaturationModifier() > points) {
                            points = item.getFoodProperties().getSaturationModifier();
                            slot = i;
                            stack = itemStack;
                        }
                        break;
                    case "Hunger":
                        if (isValidFood(item.getFoodProperties()) && item.getFoodProperties().getNutrition() > points) {
                            points = item.getFoodProperties().getNutrition();
                            slot = i;
                            stack = itemStack;
                        }
                        break;
                }
            }
        }
        return new FoodInfo(points, slot, stack);
    }

    public boolean isValidFood(FoodProperties foodComponent) {
        if (foodComponent == Foods.PUFFERFISH || foodComponent == Foods.SPIDER_EYE || foodComponent == Foods.ROTTEN_FLESH || foodComponent == Foods.POISONOUS_POTATO)
            return negativeFoods;
        else return true;
    }

    public static class FoodInfo {
        public float points;
        public int slot;
        public ItemStack itemStack;
        public Item item;

        public FoodInfo(float points, int slot, ItemStack itemStack) {
            this.points = points;
            this.slot = slot;
            this.itemStack = itemStack;
            if (itemStack != null)
                item = itemStack.getItem();
        }

    }

}
