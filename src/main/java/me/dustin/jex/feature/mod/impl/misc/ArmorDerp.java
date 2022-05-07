package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.AutoArmor;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Random;

@Feature.Manifest(category = Feature.Category.MISC, description = "Spam switch between armor in your inventory")
public class ArmorDerp extends Feature {

    @Op(name = "Delay (MS)", max = 1000, inc = 10)
    public int delay = 50;

    private boolean autoArmor;
    private final Random random = new Random();
    private final StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        ArrayList<ArmorInfo> armorInfos = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = InventoryHelper.INSTANCE.getInventory().getItem(i);
            if (stack.getItem() instanceof ArmorItem armorItem)
                armorInfos.add(new ArmorInfo(armorItem, i));
        }

        if (!armorInfos.isEmpty()) {
            if (stopWatch.hasPassed(delay)) {
                int r = random.nextInt(armorInfos.size());
                ArmorInfo armorInfo = armorInfos.get(r);
                EquipmentSlot equipmentSlot = armorInfo.armorItem().getSlot();
                int armorSlot = getArmorSlot(armorInfo.armorItem());
                int slot = armorInfo.slot();
                if (Wrapper.INSTANCE.getLocalPlayer().getItemBySlot(equipmentSlot).getItem() != Items.AIR) {
                    if (InventoryHelper.INSTANCE.isInventoryFull())
                        Wrapper.INSTANCE.getMultiPlayerGameMode().handleInventoryMouseClick(0, armorSlot, 0, ClickType.THROW, Wrapper.INSTANCE.getLocalPlayer());
                    else
                        Wrapper.INSTANCE.getMultiPlayerGameMode().handleInventoryMouseClick(0, armorSlot, 0, ClickType.QUICK_MOVE, Wrapper.INSTANCE.getLocalPlayer());
                }
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, slot < 9 ? slot + 36 : slot, ClickType.QUICK_MOVE);
                stopWatch.reset();
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    public int getArmorSlot(ArmorItem armorItem) {
        return switch (armorItem.getSlot()) {
            case FEET -> 8;
            case LEGS -> 7;
            case CHEST -> 6;
            case HEAD -> 5;
            default -> -1;
        };
    }

    @Override
    public void onEnable() {
        AutoArmor autoArmorFeature = Feature.get(AutoArmor.class);
        autoArmor = autoArmorFeature.getState();
        autoArmorFeature.setState(false);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (autoArmor) {
            Feature.get(AutoArmor.class).setState(true);
        }
        super.onDisable();
    }

    public record ArmorInfo(ArmorItem armorItem, int slot) {}
}
