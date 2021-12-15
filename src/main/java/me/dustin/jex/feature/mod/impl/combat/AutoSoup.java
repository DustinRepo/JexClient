package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Eats soup when your health gets below a certain amount.")
public class AutoSoup extends Feature {

    @Op(name = "Health", min = 1, max = 20)
    public int health = 17;

    @Op(name = "Delay (MS)", max = 1000, inc = 10)
    public int delay = 160;

    @Op(name = "Throw Delay (MS)", max = 1000, inc = 1)
    public int throwdelay = 20;
    public boolean throwing = false;
    int savedSlot;
    private Timer timer = new Timer();


    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {

        this.setSuffix(getSoups() + "");
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!timer.hasPassed(delay) || throwing)
                return;
            if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= health && getSoups() > 0) {
                if (getFirstSoup() < 9) {
                    throwing = true;

                    savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                    InventoryHelper.INSTANCE.setSlot(getFirstSoup(), true, true);
                    timer.reset();
                } else {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstSoup() < 9 ? getFirstSoup() + 36 : getFirstSoup(), SlotActionType.SWAP, 8);
                    savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                    InventoryHelper.INSTANCE.setSlot(8, true, true);
                    throwing = true;
                    timer.reset();
                }
            } else {
                throwing = false;
            }
        } else {
            if (throwing && timer.hasPassed(throwdelay)) {
                if (getFirstSoup() != -1) {
                    if (getFirstSoup() < 9) {
                        NetworkHelper.INSTANCE.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
                        InventoryHelper.INSTANCE.setSlot(savedSlot, true, true);
                        throwing = false;
                        timer.reset();
                    } else {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getFirstSoup() < 9 ? getFirstSoup() + 36 : getFirstSoup(), SlotActionType.SWAP, 8);
                    }
                } else {
                    throwing = false;

                }

            }
        }
    });

    public int getSoups() {
        int potions = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (isSoup(itemStack)) {
                potions++;
            }
        }
        return potions;
    }

    public int getFirstSoup() {
        for (int i = 0; i < 45; i++) {
            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
            if (isSoup(itemStack)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSoup(ItemStack itemStack) {
        return itemStack.getItem() == Items.MUSHROOM_STEW || itemStack.getItem() == Items.RABBIT_STEW || itemStack.getItem() == Items.BEETROOT_SOUP || itemStack.getItem() == Items.SUSPICIOUS_STEW;
    }

    @Override
    public void onDisable() {
        throwing = false;
        super.onDisable();
    }
}
