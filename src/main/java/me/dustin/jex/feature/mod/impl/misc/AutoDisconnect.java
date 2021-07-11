package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.LiteralText;

@Feature.Manifest(name = "AutoDisconnect", category = Feature.Category.MISC, description = "Automatically disconnect when your health gets below a certain value")
public class AutoDisconnect extends Feature {

    @Op(name = "Mode", all = {"Disconnect", "Chars", "Invalid Pos"})
    public String mode = "Disconnect";
    @Op(name = "Health", min = 1, max = 10)
    public int health = 5;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE && Wrapper.INSTANCE.getLocalPlayer().age >= 150) {
            if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= health) {
                switch (mode) {
                    case "Disconnect" -> {
                        Wrapper.INSTANCE.getWorld().disconnect();
                        Wrapper.INSTANCE.getMinecraft().disconnect();
                        Wrapper.INSTANCE.getMinecraft().openScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), new LiteralText("Disconnected"), new LiteralText("Disconnected because your health was below a set amount")));
                    }
                    case "Chars" -> NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket("\247r"));
                    case "Invalid Pos" -> NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, false));
                }
            }
        }
    }

}
