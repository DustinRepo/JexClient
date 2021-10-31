package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.FeatureArgumentType;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.feature.mod.core.Feature;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Cmd(name = "toggle", description = "Toggle modules.", alias = {"t"}, syntax = ".toggle <mod>")
public class CommandToggle extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal("t").redirect(dispatcher.register(literal(this.name).then(argument("feature", FeatureArgumentType.feature()).executes(this)))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Feature feature = FeatureArgumentType.getFeature(context, "feature");
        feature.toggleState();
        ConfigManager.INSTANCE.get(FeatureFile.class).write();
        ChatHelper.INSTANCE.addClientMessage(String.format("%s %s", feature.getName(), feature.getState() ? "\247a\247lON" : "\2474\247lOFF"));
        return 1;
    }
}
