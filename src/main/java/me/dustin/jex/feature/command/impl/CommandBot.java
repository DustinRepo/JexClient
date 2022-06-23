package me.dustin.jex.feature.command.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.PlayerNameArgumentType;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.AltFile;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.ConnectedServerHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.login.minecraft.MSLoginHelper;
import me.dustin.jex.helper.network.login.minecraft.MinecraftAccountManager;
import me.dustin.jex.helper.network.login.minecraft.MojangLoginHelper;
import me.dustin.jex.helper.player.bot.PlayerBot;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import java.util.UUID;

@Cmd(name = "bot", syntax = ".bot <connect/disconnect> <name>", description = "Have bots join your server")
public class CommandBot extends Command {
    @Override
    public void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal(this.name).then(literal("connect").then(argument("name", new PlayerNameArgumentType()).executes(context -> {
            String username = PlayerNameArgumentType.getPlayerName(context, "name");
                if (MinecraftAccountManager.INSTANCE.getAccounts().isEmpty())
                    ConfigManager.INSTANCE.get(AltFile.class).read();
                MinecraftAccount minecraftAccount = MinecraftAccountManager.INSTANCE.getAccount(username);
                if (minecraftAccount != null) {
                    ChatHelper.INSTANCE.addClientMessage("Logging into account...");
                    if (minecraftAccount instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                        MSLoginHelper msLoginHelper = new MSLoginHelper(microsoftAccount, true);
                        msLoginHelper.loginThread(session -> {
                            if (session == null) {
                                ChatHelper.INSTANCE.addClientMessage("Unable to login");
                                return;
                            }
                            UUID uuid = UUID.fromString(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
                            GameProfile gameProfile = new GameProfile(uuid, username);
                            PlayerBot playerBot = new PlayerBot(gameProfile, session);
                            playerBot.connect(ConnectedServerHelper.INSTANCE.getServerAddress());
                            PlayerBot.getPlayerBots().add(playerBot);
                        }, ChatHelper.INSTANCE::addClientMessage);
                    } else if (minecraftAccount instanceof MinecraftAccount.MojangAccount mojangAccount) {
                        new MojangLoginHelper(mojangAccount.getEmail(), mojangAccount.getPassword(), session -> {
                            if (session == null) {
                                ChatHelper.INSTANCE.addClientMessage("Unable to login");
                                return;
                            }
                            UUID uuid = UUID.fromString(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
                            GameProfile gameProfile = new GameProfile(uuid, username);
                            PlayerBot playerBot = new PlayerBot(gameProfile, session);
                            playerBot.connect(ConnectedServerHelper.INSTANCE.getServerAddress());
                            PlayerBot.getPlayerBots().add(playerBot);
                        }).login();
                    }
                } else {
                    ChatHelper.INSTANCE.addClientMessage("No account found in AccountManager, trying cracked");
                }

            return 1;
        }))).then(literal("disconnect").then(argument("name", new PlayerNameArgumentType()).executes(context -> {
            String name = PlayerNameArgumentType.getPlayerName(context, "name");
            PlayerBot playerBot = PlayerBot.getBot(name);
            if (playerBot != null) {
                playerBot.disconnect();
                ChatHelper.INSTANCE.addClientMessage("Removed bot named " + name);
                PlayerBot.getPlayerBots().remove(playerBot);
            }
            return 1;
        }))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
