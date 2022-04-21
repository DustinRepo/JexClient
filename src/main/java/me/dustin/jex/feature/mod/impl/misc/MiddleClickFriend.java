package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventMouseButton;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FriendFile;
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

    @EventPointer
    private final EventListener<EventMouseButton> eventMouseButtonEventListener = new EventListener<>(event -> {
        if (event.getButton() == 2 && event.getClickType() == EventMouseButton.ClickType.IN_GAME) {
            HitResult hitResult = Wrapper.INSTANCE.getMinecraft().crosshairTarget;

            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (entity instanceof PlayerEntity) {
                    String name = entity.getName().getString();
                    if (FriendHelper.INSTANCE.isFriend(entity.getName().getString())) {
                        FriendHelper.INSTANCE.removeFriend(name);
                        ChatHelper.INSTANCE.addClientMessage("Removed \247c" + name);
                    } else {
                        FriendHelper.INSTANCE.addFriend(name, name);
                        ChatHelper.INSTANCE.addClientMessage("Added \247b" + name);
                    }
                    ConfigManager.INSTANCE.get(FriendFile.class).write();
                }
            }
        }
    });
}
