package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.feature.core.Feature;
import net.minecraft.entity.player.PlayerEntity;

@Feature.Manifest(name = "NoFriendDMG", category = Feature.Category.COMBAT, description = "Prevent yourself from attacking your friends and pets")
public class NoFriendDMG extends Feature {

    @EventListener(events = {EventAttackEntity.class})
    private void runMethod(EventAttackEntity eventAttackEntity) {
        if (eventAttackEntity.getEntity() instanceof PlayerEntity) {
            if (Friend.isFriend(eventAttackEntity.getEntity().getName().asString()))
                eventAttackEntity.cancel();
        } else if (EntityHelper.INSTANCE.doesPlayerOwn(eventAttackEntity.getEntity()))
            eventAttackEntity.cancel();
    }

}
