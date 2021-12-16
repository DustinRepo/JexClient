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
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Automatically write to signs.")
public class AutoSign extends Feature {

    public Text[] signText = {new LiteralText("     "), new LiteralText(""), new LiteralText(""), new LiteralText("")};

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getWorld() == null) {
            this.setState(false);
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        if (!signText[0].asString().equalsIgnoreCase("     ") && event.getScreen() instanceof SignEditScreen) {
            event.setCancelled(true);
        }
    });

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (!signText[0].asString().equalsIgnoreCase("     ")) {
            BlockPos blockPos = ((SignEditorOpenS2CPacket) event.getPacket()).getPos();
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
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, SignEditorOpenS2CPacket.class));
}
