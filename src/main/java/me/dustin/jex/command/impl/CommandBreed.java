package me.dustin.jex.command.impl;

import me.dustin.jex.command.core.Command;
import me.dustin.jex.command.core.annotate.Cmd;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;

@Cmd(name = "Breed", description = "Instantly make all animals around you breed with your current food in hand")
public class CommandBreed extends Command {
    @Override
    public void runCommand(String command, String[] args) {
        int entCount = 0;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof AnimalEntity && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= 6) {
                if (EntityHelper.INSTANCE.canBreed((AnimalEntity)entity)) {
                    Wrapper.INSTANCE.getInteractionManager().interactEntity(Wrapper.INSTANCE.getLocalPlayer(), entity, Hand.MAIN_HAND);
                    entCount++;
                }
            }
        }
        if (entCount == 0) {
            ChatHelper.INSTANCE.addClientMessage("No breedable entities nearby");
        } else {
            ChatHelper.INSTANCE.addClientMessage("Successfully bred " + entCount + " entities");
        }
    }
}
