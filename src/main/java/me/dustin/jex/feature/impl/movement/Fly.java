package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

@Feat(name = "Fly", category = FeatureCategory.MOVEMENT, description = "Fly in survival")
public class Fly extends Feature {

    @Op(name = "Speed", min = 0.1f, max = 5f, inc = 0.1f)
    public float speed = 0.5f;
    @Op(name = "Ctrl = Down")
    public boolean ctrlDown = true;
    @Op(name = "Fly Check Bypass")
    public boolean flyCheckBypass;
    @Op(name = "Glide")
    public boolean glide = false;
    @OpChild(name = "Glide Speed", min = 0.01f, max = 2, inc = 0.01f, parent = "Glide")
    public float glideSpeed = 0.034f;

    public Fly() {
        this.setKey(GLFW.GLFW_KEY_F);
    }

    @EventListener(events = {EventMove.class, EventPacketSent.class})
    private void runMove(Event event) {
        if (event instanceof EventMove eventMove) {
            PlayerHelper.INSTANCE.setMoveSpeed((EventMove) eventMove, speed);
            eventMove.setY(0);
            boolean jumping = Wrapper.INSTANCE.getOptions().keyJump.isPressed();
            boolean sneaking = Wrapper.INSTANCE.getOptions().keySneak.isPressed() || (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && ctrlDown);

            if (jumping) {
                eventMove.setY(speed);
            } else if (sneaking) {
                eventMove.setY(-speed);
            }
            if (glide && !jumping) {
              eventMove.setY(-glideSpeed);
            }
        } else if (event instanceof EventPacketSent eventPacketSent && flyCheckBypass) {
            if (eventPacketSent.getPacket() instanceof PlayerMoveC2SPacket playerMoveC2SPacket) {
                if (Wrapper.INSTANCE.getLocalPlayer().age % 3 == 1) {
                    if (EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) > 2) {
                        PlayerMoveC2SPacket modified = new PlayerMoveC2SPacket.Full(playerMoveC2SPacket.getX(Wrapper.INSTANCE.getLocalPlayer().getX()), playerMoveC2SPacket.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) - 0.05, playerMoveC2SPacket.getZ(Wrapper.INSTANCE.getLocalPlayer().getZ()), playerMoveC2SPacket.getYaw(PlayerHelper.INSTANCE.getYaw()), playerMoveC2SPacket.getPitch(PlayerHelper.INSTANCE.getPitch()), true);
                        eventPacketSent.setPacket(modified);
                    }
                }
            }
        }

    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().setVelocity(0, 0, 0);
        super.onDisable();
    }
}
