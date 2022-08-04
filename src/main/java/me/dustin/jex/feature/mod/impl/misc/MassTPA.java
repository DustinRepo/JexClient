package me.dustin.jex.feature.mod.impl.misc;

import com.google.common.collect.Lists;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import java.util.ArrayList;
import java.util.Random;

public class MassTPA extends Feature {

    public Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay (MS)")
            .value(1000L)
            .max(5000)
            .inc(10)
            .build();

    private final StopWatch stopWatch = new StopWatch();

    public MassTPA() {
        super(Category.MISC, "Send a TPA to everyone until one is accepted");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        ArrayList<PlayerListEntry> playerList = Lists.newArrayList(Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerList());
        for (int i = 0; i < playerList.size(); i++) {
            PlayerListEntry entry = playerList.get(i);
            if (entry.getProfile().getName().equalsIgnoreCase(Wrapper.INSTANCE.getLocalPlayer().getGameProfile().getName()))
                playerList.remove(entry);
        }
        if (playerList.isEmpty())
            return;
        int size = playerList.size();
        PlayerListEntry playerListEntry = playerList.get(new Random().nextInt(size));
        ChatHelper.INSTANCE.sendCommand("tpa " + playerListEntry.getProfile().getName());
        stopWatch.reset();
    },new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        ChatMessageS2CPacket gameMessageS2CPacket = (ChatMessageS2CPacket) event.getPacket();
        if (gameMessageS2CPacket.message().getContent().getString().toLowerCase().contains("accepted your tpa")) {
            ChatHelper.INSTANCE.addClientMessage("TPA accepted. Turning off");
            setState(false);
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ChatMessageS2CPacket.class));
}
