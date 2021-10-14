package me.dustin.jex.helper.network.login;

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

public enum MojangLogin {
    INSTANCE;

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
                if (setSession)
                    ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setSession(session);
            } catch (Exception e) {

            }
        }
        return session;
    }


    public boolean login(String email, String password) throws AuthenticationException {
        if (email.contains("@")) {
            UserAuthentication auth = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()), UUID.randomUUID().toString(), Agent.MINECRAFT);
            auth.setUsername(email);
            auth.setPassword(password);
            auth.logIn();
            String username = auth.getSelectedProfile().getName();
            UUID uuid = auth.getSelectedProfile().getId();
            String token = auth.getAuthenticatedToken();
            Session session = new Session(username, uuid.toString(), token, email.contains("@") ? "mojang" : "legacy");
            ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setSession(session);
        } else {
            Session session = new Session(email, UUID.randomUUID().toString(), "fakeToken", "legacy");
            ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setSession(session);
        }
        return true;
    }


    public boolean login(MinecraftAccount.MojangAccount mojangAccount) throws AuthenticationException {
        mojangAccount.loginCount++;
        mojangAccount.lastUsed = System.currentTimeMillis();
        boolean bl = login(mojangAccount.isCracked() ? mojangAccount.getUsername() : mojangAccount.getEmail(), mojangAccount.getPassword());
        AltFile.write();
        return bl;
    }
}
