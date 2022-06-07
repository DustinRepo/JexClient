package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.MessageArgumentType;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.bot.PlayerBot;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.Vec3d;

@Cmd(name = "botcmd", syntax = ".botcmd <bot> <action> <data>", description = "Send commands to your connected bots")
public class CommandBotCmd extends Command {
    @Override
    public void registerCommand() {
        dispatcher.register(literal(this.name).then(argument("name", PlayerNameArgumentType.playerName()).then(literal("type").then(argument("message", MessageArgumentType.message()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            String message = MessageArgumentType.getMessage(context, "message").getString();
            if (name.equalsIgnoreCase("all")) {
                for (PlayerBot playerBot : PlayerBot.getPlayerBots()) {
                    playerBot.sendMessage(message);
                }
            } else {
                PlayerBot playerBot = PlayerBot.getBot(name);
                if (playerBot != null)
                    playerBot.sendMessage(message);
            }
            return 1;
        }))).then(literal("drop").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null)
                playerBot.drop(false);
            return 1;
        }).then(literal("all").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null)
                playerBot.drop(true);
            return 1;
        }))).then(literal("dropinv").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null)
                playerBot.dropInventory();
            return 1;
        })).then(literal("rotate").then(argument("yaw pitch roll", Vec3ArgumentType.vec3()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            Vec3d vec3d = Vec3ArgumentType.getVec3(context, "yaw pitch roll");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null)
                playerBot.setRotation(vec3d);
            return 1;
        }))).then(literal("use").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.use();
                playerBot.setUsing(false);
            }
            return 1;
        }).then(literal("continuous").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.setUsing(true);
                playerBot.setUseDelay(0);
            }
            return 1;
        })).then(literal("interval").then(argument("ticks", IntegerArgumentType.integer()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            int ticks = IntegerArgumentType.getInteger(context, "ticks");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.setUsing(true);
                playerBot.setUseDelay(ticks);
            }
            return 1;
        })))).then(literal("attack").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.attack();
                playerBot.setAttacking(false);
            }
            return 1;
        }).then(literal("continuous").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.setAttacking(true);
                playerBot.setAttackDelay(0);
            }
            return 1;
        })).then(literal("interval").then(argument("ticks", IntegerArgumentType.integer()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            int ticks = IntegerArgumentType.getInteger(context, "ticks");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.setAttacking(true);
                playerBot.setAttackDelay(ticks);
            }
            return 1;
        })))).then(literal("tp").executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.getPlayer().setPos(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY(), Wrapper.INSTANCE.getPlayer().getZ());
            }
            return 1;
        }))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
