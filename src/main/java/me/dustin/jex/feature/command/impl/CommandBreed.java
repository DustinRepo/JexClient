package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;

@Cmd(name = "breed", description = "Instantly make all animals around you breed with your current food in hand")
public class CommandBreed extends Command {
    @Override
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal(this.name).executes(this));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        int entCount = 0;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof AnimalEntity && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= 6) {
                if (EntityHelper.INSTANCE.canBreed((AnimalEntity)entity)) {
                    Wrapper.INSTANCE.getClientPlayerInteractionManager().interactEntity(Wrapper.INSTANCE.getLocalPlayer(), entity, Hand.MAIN_HAND);
                    entCount++;
                }
            }
        }
        if (entCount == 0) {
            ChatHelper.INSTANCE.addClientMessage("No breedable entities nearby");
        } else {
            ChatHelper.INSTANCE.addClientMessage("Successfully bred " + entCount + " entities");
        }
        return 1;
    }
}
