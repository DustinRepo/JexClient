package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.BlockStateArgumentType;
import me.dustin.jex.feature.command.core.arguments.ColorArgumentType;
import me.dustin.jex.feature.mod.impl.render.Search;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.SearchFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;

@Cmd(name = "search", description = "Add or remove blocks from Search", syntax = {".search add <blockname> <hex color (f62d3e)/random>", ".search del <blockname>", ".search list"})
public class CommandSearch extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(literal("add").then(argument("block", BlockStateArgumentType.blockState()).then(argument("color", ColorArgumentType.color()).executes(context -> {
            Block block = BlockStateArgumentType.getBlockState(context, "block").getBlockState().getBlock();
            int color = Render2DHelper.INSTANCE.hex2Rgb("0x" + Integer.toHexString(ColorArgumentType.getColor(context, "color").getColorValue())).getRGB();
            if (block != Blocks.AIR) {
                if (Search.getBlocks().containsKey(block))
                    ChatHelper.INSTANCE.addClientMessage("That block is already in the Search list!");
                else {
                    Search.getBlocks().put(block, color);
                    ChatHelper.INSTANCE.addClientMessage("\247b" + block.getName().getString() + "\2477 added to Search list");
                    ConfigManager.INSTANCE.get(SearchFile.class).write();
                }
            }
            return 1;
        })))).then(literal("del").then(argument("block", BlockStateArgumentType.blockState()).executes(context -> {
            Block block = BlockStateArgumentType.getBlockState(context, "block").getBlockState().getBlock();
            if (!Search.getBlocks().containsKey(block))
                ChatHelper.INSTANCE.addClientMessage("That block is not in the Search list!");
            else {
                Search.getBlocks().remove(block);
                ChatHelper.INSTANCE.addClientMessage("\247c" + block.getName().getString() + "\2477 deleted from Search list");
                ConfigManager.INSTANCE.get(SearchFile.class).write();
            }
            return 1;
        }))).then(literal("list").executes(this)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Search.getBlocks().keySet().forEach(block -> {
            ChatHelper.INSTANCE.addClientMessage(Registry.BLOCK.getId(block).toString());
        });
        return 1;
    }
}
