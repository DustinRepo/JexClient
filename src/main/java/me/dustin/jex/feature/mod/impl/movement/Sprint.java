package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventSetSprint;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Feature {

    public final Property<Boolean> multiDirProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Multi Dir")
            .value(false)
            .build();
    public final Property<Boolean> inInventoryProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("In inventory")
            .value(true)
            .build();

    public Sprint() {
        super(Category.MISC, "Automatically sprint", GLFW.GLFW_KEY_V);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> Wrapper.INSTANCE.getLocalPlayer().setSprinting(canSprint()), new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetSprint> eventSetSprintEventListener = new EventListener<>(event -> {
        if (!event.isSprint() && canSprint())
            event.setSprint(true);
    });

    private boolean canSprint() {
        if (!inInventoryProperty.value() && Wrapper.INSTANCE.getMinecraft().currentScreen != null)
            return false;
        return isMoving() && !Wrapper.INSTANCE.getLocalPlayer().horizontalCollision && Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() > 6;
    }

    private boolean isMoving() {
        if (multiDirProperty.value()) {
            return PlayerHelper.INSTANCE.isMoving();
        } else {
            return Wrapper.INSTANCE.getLocalPlayer().input.movementForward == 1;
        }
    }
}
