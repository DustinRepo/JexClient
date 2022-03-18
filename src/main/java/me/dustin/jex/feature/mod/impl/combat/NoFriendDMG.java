package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.entity.player.PlayerEntity;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Prevent yourself from attacking your friends and pets")
public class NoFriendDMG extends Feature {

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof PlayerEntity) {
            if (FriendHelper.INSTANCE.isFriend(event.getEntity().getName().asString()))
                event.cancel();
        } else if (EntityHelper.INSTANCE.doesPlayerOwn(event.getEntity()))
            event.cancel();
    });
}
