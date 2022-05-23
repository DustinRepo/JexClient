package me.dustin.jex.feature.command.impl;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@Cmd(name = "panic", description = "Disables all mods that are not visual incase an admin is nearby.")
public class CommandPanic extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).executes(this));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        for (Category category : Category.values()) {
            if (category != Category.VISUAL) {
                for (Feature feature : Feature.getModules(category)) {
                    if (feature.getState())
                        feature.setState(false);
                }
            }
        }
        ChatHelper.INSTANCE.addClientMessage("All non-visual mods disabled.");
        return 1;
    }
}
