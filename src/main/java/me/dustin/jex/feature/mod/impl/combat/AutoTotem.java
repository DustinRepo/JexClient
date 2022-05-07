package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.item.Items;
import me.dustin.events.core.annotate.EventPointer;

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

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        int totemCount = InventoryHelper.INSTANCE.countItems(Items.TOTEM_OF_UNDYING);
        this.setSuffix(totemCount + "");
        int firstTotem = InventoryHelper.INSTANCE.getFromHotbar(Items.TOTEM_OF_UNDYING);
        if (firstTotem == -1)
            firstTotem = InventoryHelper.INSTANCE.getFromInv(Items.TOTEM_OF_UNDYING);
        if (firstTotem == -1)
            return;
        if (needsOffhandTotem()) {
            if (Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                if (Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() != Items.AIR)
                    swappedSlot = firstTotem;
                moveTotem(firstTotem);
            }
        } else if (swappedSlot != -1) {
            moveTotem(swappedSlot);
            swappedSlot = -1;
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            swappedSlot = -1;
    }, new TickFilter(EventTick.Mode.PRE));

    public boolean needsOffhandTotem() {
        if (activateWhen.equalsIgnoreCase("Always"))
            return (replaceOffhand && Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) || Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() == Items.AIR;
        return health >= Wrapper.INSTANCE.getLocalPlayer().getHealth();
    }

    public void moveTotem(int slot) {
        if (openInventory)
            Wrapper.INSTANCE.getMinecraft().setScreen(new InventoryScreen(Wrapper.INSTANCE.getLocalPlayer()));
        InventoryHelper.INSTANCE.moveToOffhand(slot);
        if (openInventory) {
            NetworkHelper.INSTANCE.sendPacket(new ServerboundContainerClosePacket(Wrapper.INSTANCE.getLocalPlayer().containerMenu.containerId));
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
        }
    }
}
