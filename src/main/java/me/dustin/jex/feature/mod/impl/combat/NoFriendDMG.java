package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.entity.player.PlayerEntity;
import me.dustin.jex.feature.mod.core.Feature;

public class NoFriendDMG extends Feature {

    public NoFriendDMG() {
        super(Category.COMBAT, "Prevent yourself from attacking your friends and pets");
    }

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof PlayerEntity) {
            if (FriendHelper.INSTANCE.isFriend(event.getEntity().getName().getString()))
                event.cancel();
        } else if (EntityHelper.INSTANCE.doesPlayerOwn(event.getEntity()))
            event.cancel();
    });
}
