package me.dustin.jex.feature.mod.impl.combat;

import com.google.gson.JsonArray;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EndCombatS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterCombatS2CPacket;
import me.dustin.events.core.annotate.EventPointer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class AutoEZ extends Feature {

    public final Property<Long> killDetectDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Max Kill Detect Delay(MS)")
            .description("The amount of time between attacking someone and them dying to consider it a kill.")
            .value(200L)
            .min(100)
            .max(500)
            .inc(10)
            .build();

    private final Map<PlayerEntity, Long> fightingPlayers = new HashMap<>();
    private final ArrayList<String> messages = new ArrayList<>();
    private boolean isFighting;

    public AutoEZ() {
        super(Category.COMBAT, "Automatically send messages when you kill players. Configurable messages in .minecraft/JexClient/KillMessages.json");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
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
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof PlayerEntity playerEntity) {
            if (fightingPlayers.containsKey(playerEntity))
                fightingPlayers.replace(playerEntity, System.currentTimeMillis() + killDetectDelayProperty.value());
            else
                fightingPlayers.put(playerEntity, System.currentTimeMillis() + killDetectDelayProperty.value());
        }
    });

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (event.getPacket() instanceof EnterCombatS2CPacket) {
            isFighting = true;
        } else if (event.getPacket() instanceof EndCombatS2CPacket) {
            isFighting = false;
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, EnterCombatS2CPacket.class, EndCombatS2CPacket.class));

    @Override
    public void onEnable() {
        loadMessages();
        super.onEnable();
    }

    private void sendMessage(PlayerEntity playerEntity) {
        String name = playerEntity.getGameProfile().getName();
        Random random = new Random();
        String message = messages.get(random.nextInt(messages.size())).replace("%player", name);
        if (message.startsWith("/"))
            Wrapper.INSTANCE.getLocalPlayer().sendCommand(message.substring(1));
        else
            ChatHelper.INSTANCE.sendChatMessage(message);
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
            FileHelper.INSTANCE.writeFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "KillMessages.json"), output);
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
