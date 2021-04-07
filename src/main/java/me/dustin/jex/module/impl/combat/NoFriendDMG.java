package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.entity.player.PlayerEntity;

@ModClass(name = "NoFriendDMG", category = ModCategory.COMBAT, description = "Prevent yourself from attacking your friends and pets")
public class NoFriendDMG extends Module {

    @EventListener(events = {EventAttackEntity.class})
    private void runMethod(EventAttackEntity eventAttackEntity) {
        if (eventAttackEntity.getEntity() instanceof PlayerEntity) {
            if (Friend.isFriend(eventAttackEntity.getEntity().getName().asString()))
                eventAttackEntity.cancel();
        } else if (EntityHelper.INSTANCE.doesPlayerOwn(eventAttackEntity.getEntity()))
            eventAttackEntity.cancel();
    }

}
