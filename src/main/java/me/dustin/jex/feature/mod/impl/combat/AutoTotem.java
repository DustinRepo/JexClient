package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import me.dustin.events.core.annotate.EventPointer;

public class AutoTotem extends Feature {

    public final Property<ActivateTime> activateWhenProperty = new Property.PropertyBuilder<ActivateTime>(this.getClass())
            .name("When")
            .description("When the totem should go into your offhand")
            .value(ActivateTime.ALWAYS)
            .build();
    public final Property<Boolean> replaceOffhandProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Replace Current Item")
            .value(false)
            .parent(activateWhenProperty)
            .depends(parent -> parent.value() == ActivateTime.ALWAYS)
            .build();
    public final Property<Integer> healthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Health")
            .value(10)
            .min(1)
            .max(19)
            .parent(activateWhenProperty)
            .depends(parent -> parent.value() == ActivateTime.LOW_HEALTH)
            .build();
    public final Property<Boolean> openInventoryProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Open Inventory")
            .description("Tells the server you opened your inventory before moving the totem.")
            .value(false)
            .build();

    private int swappedSlot;

    public AutoTotem() {
        super(Category.COMBAT, "Keep a Totem in your offhand at all times.");
    }

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
            if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() != Items.AIR)
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
        if (activateWhenProperty.value() == ActivateTime.ALWAYS)
            return (replaceOffhandProperty.value() && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) || Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() == Items.AIR;
        return healthProperty.value() >= Wrapper.INSTANCE.getLocalPlayer().getHealth();
    }

    public void moveTotem(int slot) {
        if (openInventoryProperty.value())
            Wrapper.INSTANCE.getMinecraft().setScreen(new InventoryScreen(Wrapper.INSTANCE.getLocalPlayer()));
        InventoryHelper.INSTANCE.moveToOffhand(slot);
        if (openInventoryProperty.value()) {
            NetworkHelper.INSTANCE.sendPacket(new CloseHandledScreenC2SPacket(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.syncId));
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
        }
    }

    public enum ActivateTime {
        ALWAYS, LOW_HEALTH
    }
}
