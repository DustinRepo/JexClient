package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import org.lwjgl.glfw.GLFW;

@ModClass(name = "Fly", category = ModCategory.MOVEMENT, description = "Fly in survival")
public class Fly extends Module {

    @Op(name = "Speed", min = 0.1f, max = 5f, inc = 0.1f)
    public float speed = 0.5f;
    @Op(name = "Ctrl = Down")
    public boolean ctrlDown = true;
    @Op(name = "Glide")
    public boolean glide = false;
    @OpChild(name = "Fly Check glide", parent = "Glide")
    public boolean flyCheckGlide;
    @OpChild(name = "Glide Speed", min = 0.01f, max = 8, inc = 0.01f, parent = "Glide")
    public float glideSpeed = 0.034f;

    public Fly() {
        this.setKey(GLFW.GLFW_KEY_F);
    }

    @EventListener(events = {EventMove.class})
    private void runMove(Event event) {
        if (event instanceof EventMove) {
            EventMove eventMove = (EventMove) event;
            PlayerHelper.INSTANCE.setMoveSpeed((EventMove) eventMove, speed);
            eventMove.setY(0);
            boolean jumping = Wrapper.INSTANCE.getOptions().keyJump.isPressed();
            boolean sneaking = Wrapper.INSTANCE.getOptions().keySneak.isPressed() || (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && ctrlDown);

            if (jumping) {
                eventMove.setY(speed);
            } else if (sneaking) {
                eventMove.setY(-speed);
            }
            if (glide) {
                if (flyCheckGlide) {
                    if (Wrapper.INSTANCE.getLocalPlayer().age % 20 == 1)
                        eventMove.setY(-glideSpeed);
                    else if (Wrapper.INSTANCE.getLocalPlayer().age % 20 == 10)
                        eventMove.setY(glideSpeed);
                } else
                    eventMove.setY(-glideSpeed);
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
