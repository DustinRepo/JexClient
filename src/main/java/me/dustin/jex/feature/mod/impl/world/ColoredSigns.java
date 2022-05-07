package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Color coded signs. Use & for the color code")
public class ColoredSigns extends Feature {
    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        ServerboundSignUpdatePacket updateSignC2SPacket = (ServerboundSignUpdatePacket) event.getPacket();
        for (int i = 0; i < updateSignC2SPacket.getLines().length; i++) {
            updateSignC2SPacket.getLines()[i] = updateSignC2SPacket.getLines()[i].replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1");
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundSignUpdatePacket.class));

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        float width = Render2DHelper.INSTANCE.getScaledWidth();
        Render2DHelper.INSTANCE.fillAndBorder(event.getPoseStack(), width / 2 - 202, 5, width / 2 - 200 + FontHelper.INSTANCE.getStringWidth("Color Cheat Sheet") + 4, 236, 0xffffffff, 0x60000000, 1);
        FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), "Color Cheat Sheet", width / 2 - 200, 7, -1);
        int count = 0;
        for (ChatFormatting value : ChatFormatting.values()) {
            String first = value.getChar() == 'k' ? "" : "\247" + value.getChar();
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(),  first + "&" + value.getChar(), width / 2 - 200, 16 + count * 10, -1);
            count++;
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, SignEditScreen.class));
}
