package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

@Cmd(name = "head", syntax = ".head <player>", description = "Get the head of a chosen player.")
public class CommandHead extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(argument("player", PlayerNameArgumentType.playerName()).executes(this)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            ChatHelper.INSTANCE.addClientMessage("You must be in creative for this command!");
            return 0;
        }
        String playerName = PlayerNameArgumentType.getPlayerName(context, "player");
        ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
        NbtCompound tag = new NbtCompound();
        tag.putString("SkullOwner", playerName);
        itemStack.setNbt(tag);
        Wrapper.INSTANCE.getClientPlayerInteractionManager().clickCreativeStack(itemStack, 36);
        ChatHelper.INSTANCE.addClientMessage("Done! You now have " + playerName + "'s head.");
        return 1;
    }
}
