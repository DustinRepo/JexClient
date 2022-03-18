package me.dustin.jex.feature.mod.impl.misc;

import com.google.common.collect.Lists;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.ArrayList;
import java.util.Random;

@Feature.Manifest(description = "Send a TPA to everyone until one is accepted", category = Feature.Category.MISC)
public class MassTPA extends Feature {

    @Op(name = "Delay (MS)", max = 5000, inc = 10)
    public int delay = 1000;

    private StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delay))
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
        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket("/tpa " + playerListEntry.getProfile().getName()));
        stopWatch.reset();
    },new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        GameMessageS2CPacket gameMessageS2CPacket = (GameMessageS2CPacket) event.getPacket();
        if (gameMessageS2CPacket.getMessage().asString().toLowerCase().contains("accepted your tpa")) {
            ChatHelper.INSTANCE.addClientMessage("TPA accepted. Turning off");
            setState(false);
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, GameMessageS2CPacket.class));
}
