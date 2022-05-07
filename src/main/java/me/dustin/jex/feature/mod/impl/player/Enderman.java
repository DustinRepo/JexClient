package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Select how to deal with enderman.")
public class Enderman extends Feature {

    @Op(name = "Mode", all = {"Look At", "Look Away"})
    public String mode = "Look At";

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        setSuffix(mode);
        switch (mode.toLowerCase()) {
            case "look at":
                EnderMan lookat = getEnderman();
                if (lookat != null) {
                    RotationVector rotation = PlayerHelper.INSTANCE.rotateToEntity(lookat);
                    event.setRotation(rotation);
                }
                break;
            case "look away":
                for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
                    if (entity instanceof EnderMan) {
                        if (isPlayerStaring(Wrapper.INSTANCE.getLocalPlayer(), (EnderMan) entity)) {
                            if (PlayerHelper.INSTANCE.getPitch() > 85)
                                event.setPitch(-90);
                            else
                                event.setPitch(90);
                            break;
                        }
                    }
                }
                break;
        }
    }, Priority.SECOND, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private boolean isPlayerStaring(Player player, EnderMan endermanEntity) {
        ItemStack itemStack = InventoryHelper.INSTANCE.getInventory(player).armor.get(3);
        if (itemStack.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
            return false;
        } else {
            Vec3 vec3d = player.getViewVector(1.0F).normalize();
            Vec3 vec3d2 = new Vec3(endermanEntity.getX() - player.getX(), endermanEntity.getEyeY() - player.getEyeY(), endermanEntity.getZ() - player.getZ());
            double d = vec3d2.length();
            vec3d2 = vec3d2.normalize();
            double e = vec3d.dot(vec3d2);
            return e > 1.0D - 0.025D / d && player.hasLineOfSight(endermanEntity);
        }
    }

    private EnderMan getEnderman() {
        for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
            if (entity instanceof EnderMan endermanEntity1) {
                if (!endermanEntity1.isCreepy() && Wrapper.INSTANCE.getLocalPlayer().hasLineOfSight(endermanEntity1))
                    return endermanEntity1;
            }
        }
        return null;
    }

}
