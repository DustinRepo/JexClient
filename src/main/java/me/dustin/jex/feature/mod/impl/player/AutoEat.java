package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerUpdates;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoEat extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.SATURATION)
            .build();
    public final Property<Boolean> pressKeyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Press Key")
            .description("Press the right-click key to give animations")
            .value(false)
            .build();
    public final Property<Boolean> negativeFoodsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Eat Negative Foods")
            .value(false)
            .build();
    public final Property<Boolean> eatToRegenProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Eat To Regen")
            .description("Eat when your food is too low to regen if you don't have full health.")
            .value(false)
            .build();
    public final Property<Integer> healthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Health")
            .value(10)
            .min(1)
            .max(20)
            .inc(1)
            .parent(eatToRegenProperty)
	    .depends(parent -> (boolean)parent.value())
            .build();
    public final Property<Integer> hungerProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Hunger")
            .value(18)
            .min(18)
            .max(20)
            .inc(1)
            .parent(eatToRegenProperty)
	    .depends(parent -> (boolean)parent.value())
            .build();



    private boolean wasEating;
    private int savedSlot = 0;
    private int lastFood;
    public static boolean isEating;

    private final StopWatch baritoneStopWatch = new StopWatch();

    public AutoEat() {
        super(Category.PLAYER, "Eat food when hunger is low.");
    }

    @EventPointer
    private final EventListener<EventPlayerUpdates> eventPlayerUpdatesEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerUpdates.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer() != null && getBestFood().itemStack != null) {
                setSuffix(getBestFood().itemStack.getName().getString());
            } else {
                setSuffix("None");
            }
            if (!isEating && wasEating) {
                if (BaritoneHelper.INSTANCE.baritoneExists())
                    BaritoneHelper.INSTANCE.resume();
            }
            if (Wrapper.INSTANCE.getLocalPlayer() == null || (Feature.getState(Freecam.class) && Feature.get(Freecam.class).stealthProperty.value()) || Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
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
                    savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                    if (BaritoneHelper.INSTANCE.baritoneExists())
                        BaritoneHelper.INSTANCE.pause();
                    InventoryHelper.INSTANCE.setSlot(getBestFood().slot, true, true);
                    lastFood = Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel();
                    isEating = true;
                }
                if (lastFood != Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel()) {
                    if (lastFood < Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel()) {
                        isEating = false;
                        if (pressKeyProperty.value())
                            Wrapper.INSTANCE.getOptions().useKey.setPressed(false);
                        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
                        InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                    }
                    lastFood = Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel();
                    if (BaritoneHelper.INSTANCE.baritoneExists()) {
                        BaritoneHelper.INSTANCE.resume();
                    }
                }
                if (isEating) {
                    if (pressKeyProperty.value())
                        Wrapper.INSTANCE.getOptions().useKey.setPressed(true);
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getPlayer(), Hand.MAIN_HAND);
                }
            } else if (isEating) {
                isEating = false;
                NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
                InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                if (BaritoneHelper.INSTANCE.baritoneExists())
                    BaritoneHelper.INSTANCE.resume();
            }
            wasEating = isEating;
        }
    });

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket && isEating) {
            if (updateSelectedSlotC2SPacket.getSelectedSlot() != getBestFood().slot)
                event.cancel();
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket playerActionC2SPacket) {
            if (playerActionC2SPacket.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && isEating)
                event.cancel();
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, UpdateSelectedSlotC2SPacket.class, PlayerActionC2SPacket.class));

    private boolean needsToEat(FoodInfo foodInfo) {
        if (!eatToRegenProperty.value()) {
            return 20 - Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() >= foodInfo.item.getFoodComponent().getHunger();
        } else {
            return 20 - Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() >= foodInfo.item.getFoodComponent().getHunger() || (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= healthProperty.value() && Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() <= hungerProperty.value());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (isEating) {
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
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
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (itemStack != null && itemStack.getItem().isFood()) {
                Item item = itemStack.getItem();
                switch (modeProperty.value()) {
                    case SATURATION:
                        if (isValidFood(item.getFoodComponent()) && item.getFoodComponent().getSaturationModifier() > points) {
                            points = item.getFoodComponent().getSaturationModifier();
                            slot = i;
                            stack = itemStack;
                        }
                        break;
                    case HUNGER:
                        if (isValidFood(item.getFoodComponent()) && item.getFoodComponent().getHunger() > points) {
                            points = item.getFoodComponent().getHunger();
                            slot = i;
                            stack = itemStack;
                        }
                        break;
                }
            }
        }
        return new FoodInfo(points, slot, stack);
    }

    public boolean isValidFood(FoodComponent foodComponent) {
        if (foodComponent == FoodComponents.PUFFERFISH || foodComponent == FoodComponents.SPIDER_EYE || foodComponent == FoodComponents.ROTTEN_FLESH || foodComponent == FoodComponents.POISONOUS_POTATO)
            return negativeFoodsProperty.value();
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

    public enum Mode {
        HUNGER, SATURATION
    }
}
