package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import me.dustin.events.core.annotate.EventPointer;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Automatically eat God Apples as needed")
public class AutoGapple extends Feature {

    @Op(name = "Eat for Potions")
    public boolean eatForPotions = true;

    @Op(name = "Health", min = 5, max = 19)
    public int health = 10;
    @Op(name = "Press Key")
    public boolean pressKey = true;

    @Op(name = "Take From Inv")
    public boolean takeFromInv = true;
    @OpChild(name = "Put Into", all = {"Hotbar", "Offhand"}, parent = "Take From Inv")
    public String putInto = "Hotbar";
    @OpChild(name = "Put Back", parent = "Take From Inv")
    public boolean putBack = true;

    public int putBackSlot = -1;

    private Set<MobEffect> gappleEffects = Stream.of(
            MobEffects.FIRE_RESISTANCE,
            MobEffects.ABSORPTION,
            MobEffects.REGENERATION,
            MobEffects.DAMAGE_RESISTANCE
    ).collect(Collectors.toSet());


    private boolean isEating;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        boolean offhand = Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() == Items.ENCHANTED_GOLDEN_APPLE;
        int gappleCount = InventoryHelper.INSTANCE.countItems(Items.ENCHANTED_GOLDEN_APPLE);
        setSuffix(gappleCount + "");
        if (gappleCount == 0 && putBackSlot == -1)
            return;
        if (isEating && !shouldEatGapple()) {
            if (putBackSlot != -1) {
                if (offhand)
                    InventoryHelper.INSTANCE.moveToOffhand(putBackSlot);
                else
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, putBackSlot, ClickType.SWAP, 8);
                putBackSlot = -1;
            }
            isEating = false;
            BaritoneHelper.INSTANCE.resume();
            NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.UP));
            if (pressKey)
                Wrapper.INSTANCE.getOptions().keyUse.setDown(false);
        } else if (isEating) {
            if (pressKey)
                Wrapper.INSTANCE.getOptions().keyUse.setDown(true);
            Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getLocalPlayer(), offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        }

        if (shouldEatGapple() && !isEating) {
            if (Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() != Items.ENCHANTED_GOLDEN_APPLE) {
                int gappleHotbar = InventoryHelper.INSTANCE.getFromHotbar(Items.ENCHANTED_GOLDEN_APPLE);
                int gappleInv = InventoryHelper.INSTANCE.getFromInv(Items.ENCHANTED_GOLDEN_APPLE);

                if (gappleHotbar == -1 && takeFromInv) {
                    if (gappleInv == -1)
                        return;
                    if (putInto.equalsIgnoreCase("hotbar")) {
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, gappleInv < 9 ? gappleInv + 36 : gappleInv, ClickType.SWAP, 8);
                        gappleHotbar = 8;
                    } else
                        InventoryHelper.INSTANCE.moveToOffhand(gappleInv);
                    if (putBack)
                        putBackSlot = gappleInv;
                }
                if (gappleHotbar != -1)
                    InventoryHelper.INSTANCE.setSlot(gappleHotbar, true, true);
                if (gappleHotbar != -1 || (putInto.equalsIgnoreCase("hotbar") && takeFromInv)) {
                    isEating = true;
                    BaritoneHelper.INSTANCE.pause();
                }
            } else {
                isEating = true;
                BaritoneHelper.INSTANCE.pause();
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        boolean offhand = Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() == Items.ENCHANTED_GOLDEN_APPLE;
        if (offhand)
            return;
        if (event.getPacket() instanceof ServerboundSetCarriedItemPacket && isEating) {
            if (((ServerboundSetCarriedItemPacket) event.getPacket()).getSlot() != InventoryHelper.INSTANCE.getFromHotbar(Items.ENCHANTED_GOLDEN_APPLE))
                event.cancel();
        }
        if (event.getPacket() instanceof ServerboundPlayerActionPacket playerActionC2SPacket) {
            if (playerActionC2SPacket.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM && isEating)
                event.cancel();
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundSetCarriedItemPacket.class, ServerboundPlayerActionPacket.class));

    public boolean shouldEatGapple() {
        if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= health)
            return true;
        if (!eatForPotions)
            return false;
        return !Wrapper.INSTANCE.getLocalPlayer().getActiveEffectsMap().keySet().containsAll(gappleEffects);
    }

    public boolean isEating() {
        return isEating;
    }
}
