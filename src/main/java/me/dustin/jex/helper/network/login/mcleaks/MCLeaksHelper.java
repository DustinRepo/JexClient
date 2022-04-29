package me.dustin.jex.helper.network.login.mcleaks;

import com.google.gson.JsonObject;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.packet.EventHello;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.util.Session;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum MCLeaksHelper {
    INSTANCE;

    private static final String REDEEM_URL = "https://auth.mcleaks.net/v1/redeem";
    private static final String JOIN_SERVER_URL = "https://auth.mcleaks.net/v1/joinserver";

    public MCLeaksAccount activeAccount;
    public Session storedSession;

    public MCLeaksAccount getAccount(String token) {
        JsonObject object = new JsonObject();
        object.addProperty("token", token);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Accepts", "application/json");
        String resp = WebHelper.INSTANCE.httpRequest(REDEEM_URL, object.toString(), header, "POST").data();
        JsonObject responseObj = JsonHelper.INSTANCE.gson.fromJson(resp, JsonObject.class);
        boolean success = responseObj.get("success").getAsBoolean();
        if (success) {
            System.out.println("worked");
            return JsonHelper.INSTANCE.gson.fromJson(responseObj.get("result").toString(), MCLeaksAccount.class);
        } else
            return null;
    }

    public void setActiveAccount(MCLeaksAccount activeAccount) {
        this.activeAccount = activeAccount;
        if (storedSession == null) {
            storedSession = Wrapper.INSTANCE.getMinecraft().getSession();
        }
        Wrapper.INSTANCE.getIMinecraft().setSession(new Session(activeAccount.mcname, "", "", Optional.of(""), Optional.of(""), Session.AccountType.MOJANG));
    }

    private boolean login(String server, int port, String serverHash) {
        if (activeAccount != null) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("session", activeAccount.session);
            jsonObject.addProperty("mcname", activeAccount.mcname);
            jsonObject.addProperty("serverhash", serverHash);
            jsonObject.addProperty("server", server +":"+port);

            Map<String, String> header = new HashMap<>();
            header.put("Content-Type", "application/json");
            header.put("Accepts", "application/json");
            String resp = WebHelper.INSTANCE.httpRequest(JOIN_SERVER_URL, jsonObject.toString(), header, "GET").data();
            JexClient.INSTANCE.getLogger().info(jsonObject.toString());
            return resp != null && resp.contains("success") && resp.contains("true");
        }
        return false;
    }

    @EventPointer
    private final EventListener<EventHello> eventHelloEventListener = new EventListener<>(event -> {
        if (activeAccount != null) {
            String address = Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().address;
            int port = 25565;
            if (address.contains(":")) {
                String address1 = address.split(":")[0];
                port = Integer.parseInt(address.split(":")[1]);
                address = address1;
            }
            boolean success = login(address, port, event.getServerhash());
            if (!success) {
                event.getClientConnection().disconnect(Text.of("Bad MCLeaks response"));
                event.cancel();
            }
            JexClient.INSTANCE.getLogger().info("MCLeaks gave success to server: " + address + ":" + port + " name: " + activeAccount.mcname + " serverhash: " + event.getServerhash());
        }
    });

    public void restoreSession() {
        MCLeaksHelper.INSTANCE.activeAccount = null;
        Wrapper.INSTANCE.getIMinecraft().setSession(MCLeaksHelper.INSTANCE.storedSession);
        MCLeaksHelper.INSTANCE.storedSession = null;
    }

    public static class MCLeaksAccount {
        public String mcname;
        public String session;
    }
}
