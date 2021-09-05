package me.dustin.jex.helper.network;

import java.net.Proxy;
import java.util.UUID;

import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.file.files.AltFile;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IMinecraft;
import net.minecraft.client.util.Session;

public enum Login {

    INSTANCE;

    public Session login(String email, String password) {
        return login(email, password, true);
    }

    public Session login(String email, String password, boolean setSession) {
        Session session = null;
        if (email.contains("@")) {
            UserAuthentication auth = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()), UUID.randomUUID().toString(), Agent.MINECRAFT);
            auth.setUsername(email);
            auth.setPassword(password);
            try {
                auth.logIn();
                String username = auth.getSelectedProfile().getName();
                UUID uuid = auth.getSelectedProfile().getId();
                String token = auth.getAuthenticatedToken();
                session = new Session(username, uuid.toString(), token, email.contains("@") ? "mojang" : "legacy");
                ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setSession(session);
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        } else {
            session = new Session(email, UUID.randomUUID().toString(), "fakeToken", "legacy");
            try {
                ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setSession(session);
            } catch (Exception e) {

            }
        }
        return session;
    }


    public String loginToAccount(String email, String password) {
        if (email.contains("@")) {
            UserAuthentication auth = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()), UUID.randomUUID().toString(), Agent.MINECRAFT);
            auth.setUsername(email);
            auth.setPassword(password);
            try {
                auth.logIn();
                String username = auth.getSelectedProfile().getName();
                UUID uuid = auth.getSelectedProfile().getId();
                String token = auth.getAuthenticatedToken();
                Session session = new Session(username, uuid.toString(), token, email.contains("@") ? "mojang" : "legacy");
                ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setSession(session);
            } catch (AuthenticationException e) {
                return e.getMessage();
            }
        } else {
            Session session = new Session(email, UUID.randomUUID().toString(), "fakeToken", "legacy");
            try {
                ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setSession(session);
            } catch (Exception e) {
            }
        }
        return "Logged in as " + Wrapper.INSTANCE.getMinecraft().getSession().getUsername();
    }


    public String loginToAccount(MinecraftAccount account) {
        account.loginCount++;
        account.lastUsed = System.currentTimeMillis();
        if (account instanceof MinecraftAccount.MojangAccount mojangAccount) {
            String s = mojangAccount.isCracked() ? loginToAccount(account.getUsername(), "") : loginToAccount(mojangAccount.getEmail(), mojangAccount.getPassword());
            AltFile.write();
            return s;
        } else if (account instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
            new MicrosoftLogin(true).login(microsoftAccount.accessToken, microsoftAccount.refreshToken);
            AltFile.write();
            return "Logging in...";
        }
        return "";
    }
	 

	    /*public Session fromFile(File file)
	    {
	        if(!file.exists())
	            return null;
	        String email = FileHelper.INSTANCE.readFile(file.getPath()).get(0);
	        String password = FileHelper.INSTANCE.readFile(file.getPath()).get(1);
	        return login(email, password);
	    }*/
}
