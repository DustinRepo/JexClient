package me.dustin.jex.load.mixin.minecraft;

import com.mojang.util.UUIDTypeAdapter;
import me.dustin.jex.JexClient;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.account.MinecraftAccountManager;
import me.dustin.jex.helper.file.files.AltFile;
import me.dustin.jex.helper.network.Login;
import me.dustin.jex.helper.network.MCAPIHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.main.Main;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.UUID;

@Mixin(Main.class)
public class MixinMain {

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Session;<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), method = "main")
    private static void modifySession(Args args) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            AltFile.read();
            if (!MinecraftAccountManager.INSTANCE.getAccounts().isEmpty()) {
                MinecraftAccount mcAccount = MinecraftAccountManager.INSTANCE.getAccounts().get(0);
                if (mcAccount instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                    UUID uuid = MCAPIHelper.INSTANCE.getUUIDFromName(microsoftAccount.username);
                    if (uuid == null) {
                        JexClient.INSTANCE.getLogger().info("UUID null, can not log in");
                        return;
                    }
                    JexClient.INSTANCE.getLogger().info("Logging in to Microsoft account with name " + microsoftAccount.username);
                    args.setAll(microsoftAccount.username, UUIDTypeAdapter.fromUUID(uuid), microsoftAccount.accessToken, "mojang");
                } else if (mcAccount instanceof MinecraftAccount.MojangAccount mojangAccount) {
                    Session session = Login.INSTANCE.login(mojangAccount.getEmail(), mojangAccount.getPassword(), false);
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
