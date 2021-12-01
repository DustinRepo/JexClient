package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Keep a Totem in your offhand at all times.")
public class AutoTotem extends Feature {

    @Op(name = "When", all = {"Always", "Low Health"})
    public String activateWhen = "Always";
    @OpChild(name = "Replace Current Item", parent = "When", dependency = "Always")
    public boolean replaceOffhand = false;
    @OpChild(name = "Health", min = 5, max = 17, parent = "When", dependency = "Low Health")
    public int health = 10;
    @Op(name = "Open Inventory")
    public boolean openInventory;

    private int swappedSlot;

    @EventListener(events = {EventPlayerPackets.class})
    public void runEvent(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            int totemCount = InventoryHelper.INSTANCE.countItems(Items.TOTEM_OF_UNDYING);
            int firstTotem = InventoryHelper.INSTANCE.getFromHotbar(Items.TOTEM_OF_UNDYING);
            if (firstTotem == -1)
                firstTotem = InventoryHelper.INSTANCE.getFromInv(Items.TOTEM_OF_UNDYING);

            if (needsOffhandTotem()) {
                if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                    if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() != Items.AIR)
                        swappedSlot = firstTotem;
                    moveTotem(firstTotem);
                }
            } else if (swappedSlot != -1) {
                moveTotem(swappedSlot);
                swappedSlot = -1;
            }
            this.setSuffix(totemCount + "");
        }
    }

    @EventListener(events = {EventTick.class})
    private void tick(EventTick eventTick) {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            swappedSlot = -1;
    }

    public boolean needsOffhandTotem() {
        if (activateWhen.equalsIgnoreCase("Always"))
            return (replaceOffhand && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) || Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() == Items.AIR;
        return health >= Wrapper.INSTANCE.getLocalPlayer().getHealth();
    }

    public void moveTotem(int slot) {
        if (openInventory)
            Wrapper.INSTANCE.getMinecraft().setScreen(new InventoryScreen(Wrapper.INSTANCE.getLocalPlayer()));
        InventoryHelper.INSTANCE.moveToOffhand(slot);
        if (openInventory) {
            NetworkHelper.INSTANCE.sendPacket(new CloseHandledScreenC2SPacket(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.syncId));
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
        }
    }

    public void putTotemBack() {
        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 45, SlotActionType.PICKUP);
        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, swappedSlot < 9 ? swappedSlot + 36 : swappedSlot, SlotActionType.PICKUP);
    }
}
