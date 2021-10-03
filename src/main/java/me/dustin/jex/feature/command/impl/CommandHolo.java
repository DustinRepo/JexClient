package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

@Cmd(name = "holo", description = "Create a floating message at your location. (Creative mode)", syntax = ".holo <message>")
public class CommandHolo extends Command {

    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(argument("pos", Vec3ArgumentType.vec3()).then(argument("message", MessageArgumentType.message()).executes(context -> {
            if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
                ChatHelper.INSTANCE.addClientMessage("You must be in creative for this command!");
                return 0;
            }
            String message = MessageArgumentType.getMessage(context, "message").asString().replace("&", "\247");
            Vec3d vec = Vec3ArgumentType.getVec3(context, "pos");
            ItemStack stack = new ItemStack(Items.ARMOR_STAND);
            NbtCompound tag = new NbtCompound();
            NbtList listTag = new NbtList();
            listTag.add(NbtDouble.of(vec.getX()));
            listTag.add(NbtDouble.of(vec.getY()));
            listTag.add(NbtDouble.of(vec.getZ()));
            tag.putBoolean("Invisible", true);
            tag.putBoolean("Invulnerable", true);
            tag.putBoolean("Interpret", true);
            tag.putBoolean("NoGravity", true);
            tag.putBoolean("CustomNameVisible", true);
            tag.putString("CustomName", Text.Serializer.toJson(new LiteralText(message)));
            tag.put("Pos", listTag);
            stack.putSubTag("EntityTag", tag);
            Wrapper.INSTANCE.getInteractionManager().clickCreativeStack(stack, 36 + InventoryHelper.INSTANCE.getInventory().selectedSlot);
            return 1;
        }))).then(argument("message", MessageArgumentType.message()).executes(this)));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (!Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            ChatHelper.INSTANCE.addClientMessage("You must be in creative for this command!");
            return 0;
        }
        String message = MessageArgumentType.getMessage(context, "message").asString().replace("&", "\247");

        ItemStack stack = new ItemStack(Items.ARMOR_STAND);
        NbtCompound tag = new NbtCompound();
        NbtList listTag = new NbtList();
        listTag.add(NbtDouble.of(Wrapper.INSTANCE.getLocalPlayer().getX()));
        listTag.add(NbtDouble.of(Wrapper.INSTANCE.getLocalPlayer().getY()));
        listTag.add(NbtDouble.of(Wrapper.INSTANCE.getLocalPlayer().getZ()));
        tag.putBoolean("Invisible", true);
        tag.putBoolean("Invulnerable", true);
        tag.putBoolean("Interpret", true);
        tag.putBoolean("NoGravity", true);
        tag.putBoolean("CustomNameVisible", true);
        tag.putString("CustomName", Text.Serializer.toJson(new LiteralText(message)));
        tag.put("Pos", listTag);
        stack.putSubTag("EntityTag", tag);
        Wrapper.INSTANCE.getInteractionManager().clickCreativeStack(stack, 36 + InventoryHelper.INSTANCE.getInventory().selectedSlot);
        return 1;
    }
}
