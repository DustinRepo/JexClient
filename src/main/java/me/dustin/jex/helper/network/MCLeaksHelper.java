package me.dustin.jex.helper.network;

import com.google.gson.JsonObject;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventHello;
import me.dustin.jex.helper.file.JsonHelper;
import net.minecraft.text.LiteralText;

import java.util.HashMap;
import java.util.Map;

public enum MCLeaksHelper {
    INSTANCE;

    private static final String REDEEM_URL = "https://auth.mcleaks.net/v1/redeem";
    private static final String JOIN_SERVER_URL = "https://auth.mcleaks.net/v1/joinserver";

    public MCLeaksAccount activeAccount;

    public MCLeaksAccount getAccount(String token) {
        JsonObject object = new JsonObject();
        object.addProperty("token", token);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Accepts", "application/json");
        String resp = WebHelper.INSTANCE.sendPOST(REDEEM_URL, object.toString(), header);
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
            String resp = WebHelper.INSTANCE.sendPOST(JOIN_SERVER_URL, jsonObject.toString(), header);
            if (resp != null && resp.contains("success") && resp.contains("true"))
                return true;
        }
        return false;
    }

    @EventListener(events = {EventHello.class})
    private void receiveHello(EventHello eventHello) {
        if (activeAccount != null) {
            String address = eventHello.getClientConnection().getAddress().toString();
            int port = 25565;
            if (address.contains(":")) {
                address = address.split(":")[0];
                port = Integer.parseInt(address.split(":")[1]);
            }
            boolean success = login(address, port, eventHello.getServerhash());
            if (!success) {
                eventHello.getClientConnection().disconnect(new LiteralText("Bad MCLeaks response"));
                eventHello.cancel();
            }
        }
    }

    public static class MCLeaksAccount {
        public String mcname;
        public String session;
    }
}
