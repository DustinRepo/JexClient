package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.helper.file.files.FriendFile;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@Feature.Manifest(category = Feature.Category.MISC, description = "Middle click people to add them as friends.")
public class MiddleClickFriend extends Feature {

    @EventListener(events = {EventMouseButton.class})
    private void runMethod(EventMouseButton eventMouseButton) {
        if (eventMouseButton.getButton() == 2 && eventMouseButton.getClickType() == EventMouseButton.ClickType.IN_GAME) {
            HitResult hitResult = Wrapper.INSTANCE.getMinecraft().crosshairTarget;

            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (entity instanceof PlayerEntity) {
                    String name = entity.getName().asString();
                    if (FriendHelper.INSTANCE.isFriend(entity.getName().asString())) {
                        FriendHelper.INSTANCE.removeFriend(name);
                        ChatHelper.INSTANCE.addClientMessage("Removed \247c" + name);
                    } else {
                        FriendHelper.INSTANCE.addFriend(name, name);
                        ChatHelper.INSTANCE.addClientMessage("Added \247b" + name);
                    }
                    FriendFile.write();
                }
            }
        }
    }

}
