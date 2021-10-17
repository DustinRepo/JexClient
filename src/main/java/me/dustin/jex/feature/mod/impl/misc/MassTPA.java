package me.dustin.jex.feature.mod.impl.misc;

import com.google.common.collect.Lists;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

@Feature.Manifest(description = "Send a TPA to everyone until one is accepted", category = Feature.Category.MISC)
public class MassTPA extends Feature {

    @Op(name = "Delay (MS)", max = 5000, inc = 10)
    public int delay = 1000;

    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class, EventPacketReceive.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (!timer.hasPassed(delay))
                return;
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
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
                timer.reset();
            }
        } else if (event instanceof EventPacketReceive eventPacketReceive) {
            if (eventPacketReceive.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket) {
                if (gameMessageS2CPacket.getMessage().asString().toLowerCase().contains("accepted your tpa")) {
                    ChatHelper.INSTANCE.addClientMessage("TPA accepted. Turning off");
                    setState(false);
                }
            }
        }
    }

}
