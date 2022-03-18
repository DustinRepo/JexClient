package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.mod.impl.player.AutoDrop;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.AutoDropFile;
import me.dustin.jex.helper.misc.ChatHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

@Cmd(name = "autodrop", description = "Add or remove items from AutoDrop", syntax = {".autodrop add <item>", ".autodrop del <item>", ".autodrop list"})
public class CommandAutoDrop extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(literal("add").then(argument("item", ItemStackArgumentType.itemStack(c)).executes(ctx -> {
            Item item = ItemStackArgumentType.getItemStackArgument(ctx, "item").getItem();
            if (!AutoDrop.INSTANCE.getItems().contains(item)) {
                AutoDrop.INSTANCE.getItems().add(item);
                ChatHelper.INSTANCE.addClientMessage("\247b" + item.getName().getString() + "\2477 has been added to AutoDrop.");
                ConfigManager.INSTANCE.get(AutoDropFile.class).write();
            } else {
                ChatHelper.INSTANCE.addClientMessage("AutoDrop already contains \247c" + item.getName().getString() + "\2477.");
            }
            return 1;
        }))).then(literal("del").then(argument("item", ItemStackArgumentType.itemStack(c)).executes(ctx -> {
            Item item = ItemStackArgumentType.getItemStackArgument(ctx, "item").getItem();
            if (AutoDrop.INSTANCE.getItems().contains(item)) {
                AutoDrop.INSTANCE.getItems().remove(item);
                ChatHelper.INSTANCE.addClientMessage("\247c" + item.getName().getString() + "\2477 has been removed from AutoDrop.");
                ConfigManager.INSTANCE.get(AutoDropFile.class).write();
            } else {
                ChatHelper.INSTANCE.addClientMessage("AutoDrop does not contain \247c" + item.getName().getString() + "\2477.");
            }
            return 1;
        }))).then(literal("list").executes(context -> {
            AutoDrop.INSTANCE.getItems().forEach(item -> {
                ChatHelper.INSTANCE.addClientMessage(Registry.ITEM.getId(item).toString());
            });
            return 1;
        })));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
