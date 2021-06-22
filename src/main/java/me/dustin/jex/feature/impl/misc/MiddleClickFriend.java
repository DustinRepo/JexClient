package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.file.FriendFile;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@Feat(name = "MiddleClickFriend", category = FeatureCategory.MISC, description = "Middle click people to add them as friends.")
public class MiddleClickFriend extends Feature {

    @EventListener(events = {EventMouseButton.class})
    private void runMethod(EventMouseButton eventMouseButton) {
        if (eventMouseButton.getButton() == 2 && eventMouseButton.getClickType() == EventMouseButton.ClickType.IN_GAME) {
            HitResult hitResult = Wrapper.INSTANCE.getMinecraft().crosshairTarget;

            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (entity instanceof PlayerEntity) {
                    String name = entity.getName().asString();
                    if (Friend.isFriend(entity.getName().asString())) {
                        Friend.removeFriend(name);
                        ChatHelper.INSTANCE.addClientMessage("Removed \247c" + name);
                    } else {
                        Friend.addFriend(name, name);
                        ChatHelper.INSTANCE.addClientMessage("Added \247b" + name);
                    }
                    FriendFile.write();
                }
            }
        }
    }

}
