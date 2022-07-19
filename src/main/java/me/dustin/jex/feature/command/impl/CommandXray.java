package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.BlockStateArgumentType;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.xray.Xray;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.XrayFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.command.CommandRegistryAccess;

@Cmd(name = "xray", description = "Add or remove blocks from Xray", syntax = {".xray add <blockname>", ".xray del <blockname>", ".xray list"})
public class CommandXray extends Command {

    @Override
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal(this.name).then(literal("add").then(argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes(ctx -> {
            Block block = BlockStateArgumentType.getBlockState(ctx, "block").getBlockState().getBlock();
            if (!Xray.blockList.contains(block)) {
                Xray.blockList.add(block);
                if (Feature.get(Xray.class).getState()) {
                    Wrapper.INSTANCE.getMinecraft().worldRenderer.reload();
                }
                ChatHelper.INSTANCE.addClientMessage("\247b" + block.getName().getString() + "\2477 has been added to Xray.");
                ConfigManager.INSTANCE.get(XrayFile.class).write();
            } else {
                ChatHelper.INSTANCE.addClientMessage("Xray already contains \247c" + block.getName().getString() + "\2477.");
            }
            return 1;
        }))).then(literal("del").then(argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes(ctx -> {
            Block block = BlockStateArgumentType.getBlockState(ctx, "block").getBlockState().getBlock();
            if (Xray.blockList.contains(block)) {
                Xray.blockList.remove(block);
                if (Feature.get(Xray.class).getState()) {
                    Wrapper.INSTANCE.getMinecraft().worldRenderer.reload();
                }
                ChatHelper.INSTANCE.addClientMessage("\247c" + block.getName().getString() + "\2477 has been removed from Xray.");
                ConfigManager.INSTANCE.get(XrayFile.class).write();
            } else {
                ChatHelper.INSTANCE.addClientMessage("Xray does not contain \247c" + block.getName().getString() + "\2477.");
            }
            return 1;
        }))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
