package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;

@Feature.Manifest(category = Feature.Category.MISC, description = "Automatically disconnect when your health gets below a certain value")
public class AutoDisconnect extends Feature {

    @Op(name = "Mode", all = {"Disconnect", "Chars", "Invalid Pos"})
    public String mode = "Disconnect";
    @Op(name = "Health", min = 1, max = 10)
    public int health = 5;

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().age >= 150) {
            if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= health) {
                switch (mode) {
                    case "Disconnect" -> NetworkHelper.INSTANCE.disconnect("AutoDisconnect", Formatting.RED + "Disconnected because your health was below a set amount");
                    case "Chars" -> NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket("\247r"));
                    case "Invalid Pos" -> NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, false));
                }
            }
        }
    }, Priority.LAST);
}
