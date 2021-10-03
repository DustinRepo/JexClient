package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.FeatureArgumentType;
import me.dustin.jex.helper.file.files.FeatureFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Cmd(name = "bind", syntax = {".bind add <module> <key>", ".bind clear <module>", ".bind list"}, description = "Modify keybinds with a command. List with bind list")
public class CommandBind extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(literal("list").executes(this)).then(literal("set").then(argument("feature", FeatureArgumentType.feature()).then(argument("key", StringArgumentType.string()).executes(context -> {
            Feature feature = FeatureArgumentType.getFeature(context, "feature");
            String keyName = StringArgumentType.getString(context, "key");
            int key = KeyboardHelper.INSTANCE.getKeyFromName(keyName);
            if (key == -1) {
                ChatHelper.INSTANCE.addClientMessage("Key not found.");
                return 0;
            }
            feature.setKey(key);
            ChatHelper.INSTANCE.addClientMessage("\247b" + feature.getName() + " \2477has been bound to \247b" + keyName);
            FeatureFile.write();
            return 1;
        })))).then(literal("clear").then(argument("feature", FeatureArgumentType.feature()).executes(context -> {
            Feature feature = FeatureArgumentType.getFeature(context, "feature");
            feature.setKey(0);
            ChatHelper.INSTANCE.addClientMessage("\247b" + feature.getName() + " \2477has been unbound");
            FeatureFile.write();
            return 1;
        }))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ChatHelper.INSTANCE.addClientMessage("Listing keybinds.");
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (feature.getKey() != 0) {
                ChatHelper.INSTANCE.addClientMessage("\247b" + feature.getName() + "\247f: \2477" + KeyboardHelper.INSTANCE.getKeyName(feature.getKey()));
            }
        }
        return 1;
    }
}
