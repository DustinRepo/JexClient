package me.dustin.jex.feature.mod.impl.combat;

import com.google.gson.JsonArray;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.misc.EventSetScreen;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.client.gui.screen.DeathScreen;
import me.dustin.events.core.annotate.EventPointer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AutoCope extends Feature {

    private final ArrayList<String> messages = new ArrayList<>();

    public AutoCope() {
        super(Category.COMBAT, "Automatically send messages when you die to a player. Configurable messages in .minecraft/JexClient/CopeMessages.json");
    }

    @EventPointer
    private final EventListener<EventSetScreen> eventSetScreenEventListener = new EventListener<>(event -> {
        if (event.getScreen() instanceof DeathScreen)
            sendMessage();
    });

    @Override
    public void onEnable() {
        loadMessages();
        super.onEnable();
    }

    private void sendMessage() {
        Random random = new Random();
        String message = messages.get(random.nextInt(messages.size()));
        if (message.startsWith("/"))
            Wrapper.INSTANCE.getLocalPlayer().sendCommand(message.substring(1));
        else
            ChatHelper.INSTANCE.sendChatMessage(message);
    }

    private void loadMessages() {
        messages.clear();
        File messageFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "CopeMessages.json");
        String jsonString = "";
        if (!messageFile.exists()) {
            FileHelper.INSTANCE.createFile(ModFileHelper.INSTANCE.getJexDirectory(), "CopeMessages.json");
            jsonString = defaultJson;
            ArrayList<String> output = new ArrayList<>();
            Collections.addAll(output, jsonString.split("\n"));
            FileHelper.INSTANCE.writeFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "CopeMessages.json"), output);
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
        }catch (Exception e) {
            ChatHelper.INSTANCE.addClientMessage("Error in json file.");
            ChatHelper.INSTANCE.addClientMessage(e.getMessage());
            e.printStackTrace();
        }
    }

    private String defaultJson = "[\n" +
            "  \"shutup pingfriend\",\n" +
            "  \"hole camp more will ya\",\n" +
            "  \"regearing? bad form!\",\n" +
            "  \"pop more totems loser\",\n" +
            "  \"i'm playing from my microwave\",\n" +
            "  \"i have satellite internet\",\n" +
            "  \"my cat keeps walking on my keyboard\",\n" +
            "  \"and my sister won't stop asking me to use the computer\",\n" +
            "  \"the sun was in my eyes\",\n" +
            "  \"i'm sleep deprived, been up for 392 hours\",\n" +
            "  \"java wanted to update mid-fight\",\n" +
            "  \"my virus scanner started when we engaged, laaaaaaag!\",\n" +
            "  \"i spilled my water on the cpu\",\n" +
            "  \"my dog has diabetes and i had to give him his injection\",\n" +
            "  \"i have cerebral palsy but it only flares up when i'm cpvping\"\n" +
            "]";
}
