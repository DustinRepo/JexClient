package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Automatically write to signs.")
public class AutoSign extends Feature {

    public Component[] signText = {Component.nullToEmpty("     "), Component.nullToEmpty(""), Component.nullToEmpty(""), Component.nullToEmpty("")};

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getWorld() == null) {
            this.setState(false);
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        if (!signText[0].getString().equalsIgnoreCase("     ") && event.getScreen() instanceof SignEditScreen) {
            event.setCancelled(true);
        }
    });

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (!signText[0].getString().equalsIgnoreCase("     ")) {
            BlockPos blockPos = ((ClientboundOpenSignEditorPacket) event.getPacket()).getPos();
            SignBlockEntity signBlockEntity = (SignBlockEntity) Wrapper.INSTANCE.getWorld().getBlockEntity(blockPos);
            if (signBlockEntity == null)
                return;
            signBlockEntity.setEditable(true);
            signBlockEntity.setAllowedPlayerEditor(Wrapper.INSTANCE.getLocalPlayer().getUUID());
            for (int i = 0; i < 4; i++) {
                signBlockEntity.setMessage(i, Component.nullToEmpty(signText[i].getString().replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1")));
            }
            if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getWorld() != null)
                NetworkHelper.INSTANCE.sendPacket(new ServerboundSignUpdatePacket(signBlockEntity.getBlockPos(), signBlockEntity.getMessage(0, false).getString(), signBlockEntity.getMessage(1, false).getString(), signBlockEntity.getMessage(2, false).getString(), signBlockEntity.getMessage(3, false).getString()));
            signBlockEntity.setChanged();
            event.cancel();
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ClientboundOpenSignEditorPacket.class));
}
