package me.dustin.jex.load.mixin.minecraft;

import com.mojang.util.UUIDTypeAdapter;
import me.dustin.jex.JexClient;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.account.MinecraftAccountManager;
import me.dustin.jex.helper.file.files.AltFile;
import me.dustin.jex.helper.network.login.MojangLogin;
import me.dustin.jex.helper.network.login.MicrosoftLogin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.main.Main;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Main.class)
public class MixinMain {

    @ModifyArgs(at = @At(value = "INVOKE", target = "net/minecraft/client/util/Session.<init> (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Optional;Ljava/util/Optional;Lnet/minecraft/client/util/Session$AccountType;)V"), method = "main")
    private static void modifySession(Args args) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            AltFile.read();
            if (!MinecraftAccountManager.INSTANCE.getAccounts().isEmpty()) {
                MinecraftAccount mcAccount = MinecraftAccountManager.INSTANCE.getAccounts().get(0);
                if (mcAccount instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                    String uuid = microsoftAccount.uuid;
                    if (uuid == null || uuid.equalsIgnoreCase("null")) {
                        JexClient.INSTANCE.getLogger().info("UUID null, can not log in");
                        return;
                    }
                    JexClient.INSTANCE.getLogger().info("Logging in to Microsoft account with name " + microsoftAccount.username);
                    if (new MicrosoftLogin(microsoftAccount).login()) {
                        new MicrosoftLogin(microsoftAccount.getEmail(), microsoftAccount.getPassword(), microsoftAccount.accessToken, microsoftAccount.accessToken, true).login();
                        args.set(0, microsoftAccount.username);
                        args.set(1, uuid);
                        args.set(2, microsoftAccount.accessToken);
                        args.set(5, Session.AccountType.MSA);
                    }
                } else if (mcAccount instanceof MinecraftAccount.MojangAccount mojangAccount) {
                    Session session = MojangLogin.INSTANCE.login(mojangAccount.getEmail(), mojangAccount.getPassword(), false);
                    if (session == null)
                        return;
                    JexClient.INSTANCE.getLogger().info("Logging in to Mojang account with name " + session.getUsername());
                    args.set(0, session.getUsername());
                    args.set(1, session.getUuid());
                    args.set(2, session.getAccessToken());
                    args.set(5, session.getAccountType());
                } else {
                    JexClient.INSTANCE.getLogger().info("Account not recognized, can not log in.");
                }
            } else {
                JexClient.INSTANCE.getLogger().info("Accounts empty, can not log in.");
            }
        }
    }

}
