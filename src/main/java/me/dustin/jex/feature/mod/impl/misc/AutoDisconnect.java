package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;

public class AutoDisconnect extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .description("The mode used to disconnect you.")
            .value(Mode.DISCONNECT)
            .build();
    public final Property<Integer> healthProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Health")
            .value(5)
            .min(1)
            .max(10)
            .build();

    public AutoDisconnect() {
        super(Category.MISC, "");
    }

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().age >= 150) {
            if (Wrapper.INSTANCE.getLocalPlayer().getHealth() <= healthProperty.value()) {
                switch (modeProperty.value()) {
                    case DISCONNECT -> NetworkHelper.INSTANCE.disconnect("AutoDisconnect", Formatting.RED + "Disconnected because your health was below a set amount");
                    case CHARS -> ChatHelper.INSTANCE.sendChatMessage("\247r");
                    case INVALID_POS -> NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, false));
                }
            }
        }
    }, Priority.LAST, new TickFilter(EventTick.Mode.PRE));

    public enum Mode {
        DISCONNECT, CHARS, INVALID_POS
    }
}
