package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.file.FriendFile;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@ModClass(name = "MiddleClickFriend", category = ModCategory.MISC, description = "Middle click people to add them as friends.")
public class MiddleClickFriend extends Module {

    private boolean pressed;

    @EventListener(events = {EventTick.class})
    private void runMethod(EventTick eventTick) {
        boolean currentlyPressed = MouseHelper.INSTANCE.isMouseButtonDown(2);
        if (pressed && !currentlyPressed) {
            pressed = false;
        }
        if (!pressed && currentlyPressed) {
            HitResult hitResult = Wrapper.INSTANCE.getMinecraft().crosshairTarget;

            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                System.out.println("entity found");
                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (entity instanceof PlayerEntity) {
                    String name = entity.getName().asString();
                    if (Friend.isFriend(entity.getName().asString())) {
                        Friend.removeFriend(name);
                        ChatHelper.INSTANCE.addClientMessage("Removed \247c" + name);
                        FriendFile.write();
                    } else {
                        Friend.addFriend(name, name);
                        ChatHelper.INSTANCE.addClientMessage("Added \247b" + name);
                        FriendFile.write();
                    }
                }
            }
            pressed = true;
        }
    }

}
