package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.client.render.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

@Feat(name = "Blink", category = FeatureCategory.PLAYER, description = "Delay your movements to the server, making it seem like you teleported.")
public class Blink extends Feature {

    private ArrayList<PlayerMoveC2SPacket> packets = new ArrayList<>();

    @EventListener(events = {EventPacketSent.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (event instanceof EventPacketSent eventPacketSent) {
            if (Wrapper.INSTANCE.getLocalPlayer() == null) {
                packets.clear();
                this.setState(false);
                return;
            }
            if (eventPacketSent.getPacket() instanceof PlayerMoveC2SPacket) {
                if (PlayerHelper.INSTANCE.isMoving()) {
                    packets.add((PlayerMoveC2SPacket)eventPacketSent.getPacket());
                }
                eventPacketSent.cancel();
            }
        } else if (event instanceof EventRender3D) {
            if (packets.size() > 1) {
                Color color = ColorHelper.INSTANCE.getColor(ColorHelper.INSTANCE.getClientColor());
                Render3DHelper.INSTANCE.setup3DRender(true);
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                for (PlayerMoveC2SPacket packet : packets) {
                    Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(packet.getX(0), packet.getY(0), packet.getZ(0)));
                    bufferBuilder.vertex(renderPos.getX(), renderPos.getY(), renderPos.getZ()).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
                }
                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);
                Render3DHelper.INSTANCE.end3DRender();
            }
            this.setSuffix(String.valueOf(packets.size()));
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        packets.forEach(NetworkHelper.INSTANCE::sendPacket);
        packets.clear();
    }
}
