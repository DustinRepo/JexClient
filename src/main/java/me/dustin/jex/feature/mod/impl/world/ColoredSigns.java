package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.DrawScreenFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.Formatting;

public class ColoredSigns extends Feature {

    public ColoredSigns() {
        super(Category.WORLD, "Color coded signs. Use & for the color code");
    }

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        UpdateSignC2SPacket updateSignC2SPacket = (UpdateSignC2SPacket) event.getPacket();
        for (int i = 0; i < updateSignC2SPacket.getText().length; i++) {
            updateSignC2SPacket.getText()[i] = updateSignC2SPacket.getText()[i].replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1");
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, UpdateSignC2SPacket.class));

    @EventPointer
    private final EventListener<EventDrawScreen> eventDrawScreenEventListener = new EventListener<>(event -> {
        float width = Render2DHelper.INSTANCE.getScaledWidth();
        Render2DHelper.INSTANCE.fillAndBorder(event.getPoseStack(), width / 2 - 202, 5, width / 2 - 200 + FontHelper.INSTANCE.getStringWidth("Color Cheat Sheet") + 4, 236, 0xffffffff, 0x60000000, 1);
        FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(), "Color Cheat Sheet", width / 2 - 200, 7, -1);
        int count = 0;
        for (Formatting value : Formatting.values()) {
            String first = value.getCode() == 'k' ? "" : "\247" + value.getCode();
            FontHelper.INSTANCE.drawWithShadow(event.getPoseStack(),  first + "&" + value.getCode(), width / 2 - 200, 16 + count * 10, -1);
            count++;
        }
    }, new DrawScreenFilter(EventDrawScreen.Mode.POST, SignEditScreen.class));
}
