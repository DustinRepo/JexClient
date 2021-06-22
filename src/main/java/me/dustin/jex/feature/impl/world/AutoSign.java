package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

@Feat(name = "AutoSign", category = FeatureCategory.WORLD, description = "Automatically write to signs.")
public class AutoSign extends Feature {

    public Text[] signText = {new LiteralText("     "), new LiteralText(""), new LiteralText(""), new LiteralText("")};

    @EventListener(events = {EventTick.class, EventDisplayScreen.class, EventPacketReceive.class})
    private void runMethod(Event event) {
        if (event instanceof EventTick) {
            if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getWorld() == null) {
                this.setState(false);
            }
        } else if (event instanceof EventDisplayScreen) {
            if (!signText[0].asString().equalsIgnoreCase("     ") && ((EventDisplayScreen) event).getScreen() instanceof SignEditScreen) {
                event.setCancelled(true);
            }
        } else if (event instanceof EventPacketReceive eventPacketReceive) {
            if (eventPacketReceive.getPacket() instanceof SignEditorOpenS2CPacket && !signText[0].asString().equalsIgnoreCase("     ")) {
                BlockPos blockPos = ((SignEditorOpenS2CPacket) eventPacketReceive.getPacket()).getPos();
                SignBlockEntity signBlockEntity = (SignBlockEntity) Wrapper.INSTANCE.getWorld().getBlockEntity(blockPos);
                if (signBlockEntity == null)
                    return;
                signBlockEntity.setEditable(true);
                signBlockEntity.setEditor(Wrapper.INSTANCE.getLocalPlayer().getUuid());
                for (int i = 0; i < 4; i++) {
                    signBlockEntity.setTextOnRow(i, new LiteralText(signText[i].getString().replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1")));
                }
                if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getWorld() != null)
                    NetworkHelper.INSTANCE.sendPacket(new UpdateSignC2SPacket(signBlockEntity.getPos(), signBlockEntity.getTextOnRow(0, false).asString(), signBlockEntity.getTextOnRow(1, false).asString(), signBlockEntity.getTextOnRow(2, false).asString(), signBlockEntity.getTextOnRow(3, false).asString()));
                signBlockEntity.markDirty();
                event.cancel();
            }
        }
    }
}
