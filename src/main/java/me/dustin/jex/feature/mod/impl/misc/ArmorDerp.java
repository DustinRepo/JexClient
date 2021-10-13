package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.AutoArmor;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Feature.Manifest(name = "ArmorDerp", category = Feature.Category.MISC, description = "Spam switch between armor in your inventory")
public class ArmorDerp extends Feature {

    @Op(name = "Delay (MS)", max = 1000, inc = 10)
    public int delay = 50;

    private boolean autoArmor;
    private final Random random = new Random();
    private final Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            ArrayList<ArmorInfo> armorInfos = new ArrayList<>();
            for (int i = 0; i < 36; i++) {
                ItemStack stack = InventoryHelper.INSTANCE.getInventory().getStack(i);
                if (stack.getItem() instanceof ArmorItem armorItem)
                    armorInfos.add(new ArmorInfo(armorItem, i));
            }

            if (!armorInfos.isEmpty()) {
                if (timer.hasPassed(delay)) {
                    int r = random.nextInt(armorInfos.size());
                    ArmorInfo armorInfo = armorInfos.get(r);
                    EquipmentSlot equipmentSlot = armorInfo.armorItem().getSlotType();
                    int armorSlot = getArmorSlot(armorInfo.armorItem());
                    int slot = armorInfo.slot();
                    if (Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(equipmentSlot).getItem() != Items.AIR) {
                        if (InventoryHelper.INSTANCE.isInventoryFull())
                            Wrapper.INSTANCE.getInteractionManager().clickSlot(0, armorSlot, 0, SlotActionType.THROW, Wrapper.INSTANCE.getLocalPlayer());
                        else
                            Wrapper.INSTANCE.getInteractionManager().clickSlot(0, armorSlot, 0, SlotActionType.QUICK_MOVE, Wrapper.INSTANCE.getLocalPlayer());
                    }
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, slot < 9 ? slot + 36 : slot, SlotActionType.QUICK_MOVE);
                    timer.reset();
                }
            }
        }
    }

    public int getArmorSlot(ArmorItem armorItem) {
        return switch (armorItem.getSlotType()) {
            case FEET -> 8;
            case LEGS -> 7;
            case CHEST -> 6;
            case HEAD -> 5;
            default -> -1;
        };
    }

    @Override
    public void onEnable() {
        Feature f = Feature.get(AutoArmor.class);
        if (f != null) {
            autoArmor = f.getState();
            f.setState(false);
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (autoArmor) {
            Feature f = Feature.get(AutoArmor.class);
            if (f != null)
                f.setState(true);
        }
        super.onDisable();
    }

    public record ArmorInfo(ArmorItem armorItem, int slot) {}
}
