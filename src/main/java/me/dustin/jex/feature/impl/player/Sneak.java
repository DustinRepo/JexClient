package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.lwjgl.glfw.GLFW;

@Feat(name = "Sneak", category = FeatureCategory.PLAYER, description = "Sneak around to hide your nametag")
public class Sneak extends Feature {

    public Sneak() {
        this.setKey(GLFW.GLFW_KEY_Z);
    }

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (Wrapper.INSTANCE.getLocalPlayer().isRiding())
            return;
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        } else {
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        super.onDisable();
    }
}
