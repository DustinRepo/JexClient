package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.JexClient;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;
import me.dustin.jex.helper.network.login.minecraft.MicrosoftLogin;
import me.dustin.jex.helper.network.login.minecraft.MojangLogin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.main.Main;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Mixin(Main.class)
public class MixinMain {

    private static MinecraftAccount mcAccount;

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Session;<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"), method = "main")
    private static void modifySession(Args args) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "dev-login.yml"));
            if (parsedYaml == null || parsedYaml.isEmpty())
                return;
            String username = (String)parsedYaml.get("username");
            String email = (String)parsedYaml.get("email");
            String password = (String)parsedYaml.get("password");
            String accountType = (String)parsedYaml.get("account-type");
            if (accountType.equalsIgnoreCase("msa")) {
                mcAccount = new MinecraftAccount.MicrosoftAccount(username, email, password, "", "", "");
            } else {
                mcAccount = new MinecraftAccount.MojangAccount(username, email, password);
            }
            if (mcAccount instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                Session session = new MicrosoftLogin(microsoftAccount, session1 -> {}).loginNoThread();
                if (session != null) {
                    JexClient.INSTANCE.getLogger().info("Logging in to Microsoft account with name " + microsoftAccount.username);
                    args.setAll(session.getUsername(), session.getUuid(), session.getAccessToken(), "msa");
                } else
                    JexClient.INSTANCE.getLogger().info("Unable to login");
            } else if (mcAccount instanceof MinecraftAccount.MojangAccount mojangAccount) {
                Session session = MojangLogin.login(mojangAccount.getEmail(), mojangAccount.getPassword());
                if (session != null) {
                    JexClient.INSTANCE.getLogger().info("Logging in to Mojang account with name " + session.getUsername());
                    args.setAll(session.getUsername(), session.getUuid(), session.getAccessToken(), "mojang");
                } else
                    JexClient.INSTANCE.getLogger().info("Unable to login");
            } else {
                JexClient.INSTANCE.getLogger().info("Account not recognized, can not log in.");
            }
        } else {
            JexClient.INSTANCE.getLogger().info("Accounts empty, can not log in.");
        }
    }
}
