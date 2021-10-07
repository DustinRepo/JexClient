package me.dustin.jex.feature.mod.impl.combat;

import com.google.gson.JsonArray;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

@Feature.Manifest(name = "AutoEZ", category = Feature.Category.COMBAT, description = "Automatically send messages when you kill players. Configurable messages in .minecraft/JexClient/KillMessages.json")
public class AutoEZ extends Feature {

    @Op(name = "Max Kill Detect Delay(MS)", min = 100, max = 500, inc = 10)
    public int killDetectDelay = 200;

    private ArrayList<String> messages = new ArrayList<>();

    private boolean isFighting;
    private Map<PlayerEntity, Long> fightingPlayers = new HashMap<>();

    @EventListener(events = {EventPlayerPackets.class, EventPacketReceive.class, EventAttackEntity.class})
    private void runMethod(Event event) {
        if (event instanceof EventPacketReceive eventPacketReceive) {
            if (eventPacketReceive.getPacket() instanceof EnterCombatS2CPacket) {
                isFighting = true;
            } else if (eventPacketReceive.getPacket() instanceof EndCombatS2CPacket) {
                isFighting = false;
            }
        } else if (event instanceof EventAttackEntity eventAttackEntity) {
            if (eventAttackEntity.getEntity() instanceof PlayerEntity playerEntity) {
                if (fightingPlayers.containsKey(playerEntity))
                    fightingPlayers.replace(playerEntity, System.currentTimeMillis() + killDetectDelay);
                else
                    fightingPlayers.put(playerEntity, System.currentTimeMillis() + killDetectDelay);
            }
        } else if (event instanceof EventPlayerPackets eventPlayerPackets && eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            for (int i = 0; i < fightingPlayers.size(); i++) {
                PlayerEntity player = new ArrayList<>(fightingPlayers.keySet()).get(i);
                long time = fightingPlayers.get(player);
                if (time <= System.currentTimeMillis()) {
                    fightingPlayers.remove(player);
                    continue;
                }
                if (player.getHealth() <= 0 || player.isDead() && isFighting) {
                    sendMessage(player);
                    fightingPlayers.remove(player);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        loadMessages();
        super.onEnable();
    }

    private void sendMessage(PlayerEntity playerEntity) {
        String name = playerEntity.getGameProfile().getName();
        Random random = new Random();
        String message = messages.get(random.nextInt(messages.size())).replace("%player", name);
        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(message));
    }

    private void loadMessages() {
        messages.clear();
        File messageFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "KillMessages.json");
        String jsonString = "";
        if (!messageFile.exists()) {
            FileHelper.INSTANCE.createFile(ModFileHelper.INSTANCE.getJexDirectory(), "KillMessages.json");
            jsonString = defaultJson;
            ArrayList<String> output = new ArrayList<>();
            Collections.addAll(output, jsonString.split("\n"));
            FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), "KillMessages.json", output);
        } else {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(messageFile), "UTF8"));
                String inString;
                while ((inString = in.readLine()) != null) {
                    sb.append(inString);
                    sb.append("\n");
                }
                in.close();
                jsonString = sb.toString();
            } catch (Exception e) {
                ChatHelper.INSTANCE.addClientMessage("Error reading file.");
                e.printStackTrace();
            }
        }
        try {
            JsonArray jsonArray = JsonHelper.INSTANCE.prettyGson.fromJson(jsonString, JsonArray.class);
            for (int i = 0; i < jsonArray.size(); i++) {
                messages.add(jsonArray.get(i).getAsString());
            }
        } catch (Exception e) {
            ChatHelper.INSTANCE.addClientMessage("Error in json file.");
            ChatHelper.INSTANCE.addClientMessage(e.getMessage());
            e.printStackTrace();
        }
    }

    private String defaultJson = "[\n" +
            "  \"%player died to Jex Client\",\n" +
            "  \"get better kid\",\n" +
            "  \"wow I didn't even use a totem. you suck, %player\",\n" +
            "  \"cope, nn\",\n" +
            "  \"ez\"\n" +
            "]";
}
