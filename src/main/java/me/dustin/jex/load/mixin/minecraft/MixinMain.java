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

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Session;<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), method = "main")
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
                    new MicrosoftLogin(false).refreshTokens(microsoftAccount);
                    args.setAll(microsoftAccount.username, uuid, microsoftAccount.accessToken, "mojang");
                } else if (mcAccount instanceof MinecraftAccount.MojangAccount mojangAccount) {
                    Session session = MojangLogin.INSTANCE.login(mojangAccount.getEmail(), mojangAccount.getPassword(), false);
                    JexClient.INSTANCE.getLogger().info("Logging in to Mojang account with name " + session.getUsername());
                    args.setAll(session.getUsername(), UUIDTypeAdapter.fromUUID(session.getProfile().getId()), session.getAccessToken(), "mojang");
                } else {
                    JexClient.INSTANCE.getLogger().info("Account not recognized, can not log in.");
                }
            } else {
                JexClient.INSTANCE.getLogger().info("Accounts empty, can not log in.");
            }
        }
    }

}
