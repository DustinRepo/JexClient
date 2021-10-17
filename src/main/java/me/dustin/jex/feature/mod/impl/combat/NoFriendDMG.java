package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.entity.player.PlayerEntity;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Prevent yourself from attacking your friends and pets")
public class NoFriendDMG extends Feature {

    @EventListener(events = {EventAttackEntity.class})
    private void runMethod(EventAttackEntity eventAttackEntity) {
        if (eventAttackEntity.getEntity() instanceof PlayerEntity) {
            if (FriendHelper.INSTANCE.isFriend(eventAttackEntity.getEntity().getName().asString()))
                eventAttackEntity.cancel();
        } else if (EntityHelper.INSTANCE.doesPlayerOwn(eventAttackEntity.getEntity()))
            eventAttackEntity.cancel();
    }

}
