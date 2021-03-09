package me.dustin.jex.module.impl.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.io.File;
import java.util.ArrayList;

@ModClass(name = "Announcer", category = ModCategory.MISC, description = "Fastest way to get muted! Fully customizable with files in the Jex folder")
public class Announcer extends Module {

    @Op(name = "Message Delay", min = 50, max = 5000, inc = 10)
    public int messageDelay = 1000;

    private File announcerFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "announcer.json");

    private ArrayList<String> joinMessages = new ArrayList<>();
    private ArrayList<String> leaveMessages = new ArrayList<>();

    private ArrayList<String> playerNames = new ArrayList<>();

    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() != EventPlayerPackets.Mode.PRE)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().age < 30 || !timer.hasPassed(messageDelay)) {
            copyPlayerNames();
            return;
        }
        ArrayList<String> currentNames = getPlayerNames();
        for (String name : playerNames) {
            if (!currentNames.contains(name) && !name.equalsIgnoreCase(Wrapper.INSTANCE.getLocalPlayer().getGameProfile().getName())) {//someone left
                int rand = ClientMathHelper.INSTANCE.getRandom(leaveMessages.size());
                NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(leaveMessages.get(rand).replace("%player", name)));
                timer.reset();
            }
        }
        for (String name : currentNames) {
            if (!playerNames.contains(name) && !name.equalsIgnoreCase(Wrapper.INSTANCE.getLocalPlayer().getGameProfile().getName())) {//someone joined
                int rand = ClientMathHelper.INSTANCE.getRandom(joinMessages.size());
                NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(joinMessages.get(rand).replace("%player", name)));
                timer.reset();
            }
        }
        copyPlayerNames();
    }

    private ArrayList<String> getPlayerNames() {
        ArrayList<String> names = new ArrayList<>();
        for (PlayerListEntry playerListEntry : Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerList()) {
            if (playerListEntry == null || playerListEntry.getProfile() == null || playerListEntry.getProfile() == Wrapper.INSTANCE.getLocalPlayer().getGameProfile())
                continue;
            names.add(playerListEntry.getProfile().getName());
        }
        return names;
    }

    private void copyPlayerNames() {
        playerNames.clear();
        for (PlayerListEntry playerListEntry : Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerList()) {
            if (playerListEntry == null || playerListEntry.getProfile() == null || playerListEntry.getProfile() == Wrapper.INSTANCE.getLocalPlayer().getGameProfile())
                continue;
            playerNames.add(playerListEntry.getProfile().getName());
        }
    }

    @Override
    public void onEnable() {
        loadFiles();
        copyPlayerNames();
        super.onEnable();
    }

    public void loadFiles() {
     if (!announcerFile.exists())
         createDefaultAnnouncerFile();
     String read = "";
     for (String s : FileHelper.INSTANCE.readFile(ModFileHelper.INSTANCE.getJexDirectory(), "announcer.json")) {
         read += s;
     }
     try {
         JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(read, JsonObject.class);
         JsonArray joinMessageArray = object.getAsJsonArray("JoinMessages");
         JsonArray leaveMessageArray = object.getAsJsonArray("LeaveMessages");
         joinMessages.clear();
         leaveMessages.clear();
         for (int i = 0; i < joinMessageArray.size(); i++) {
            String s = joinMessageArray.get(i).getAsString();
            joinMessages.add(s);
         }
         for (int i = 0; i < leaveMessageArray.size(); i++) {
             String s = leaveMessageArray.get(i).getAsString();
             leaveMessages.add(s);
         }
     } catch (Exception e) {
         ChatHelper.INSTANCE.addClientMessage("Error in json file.");
         ChatHelper.INSTANCE.addClientMessage(e.getMessage());
         e.printStackTrace();
     }
    }

    private void createDefaultAnnouncerFile() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String s : defaultAnnounce.split("\n"))
            arrayList.add(s);
        FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), "announcer.json", arrayList);
    }

    public String defaultAnnounce =
            "{\n" +
            "\t\"JoinMessages\": [\n" +
            "\t\t\"Hello, %player.\",\n" +
            "\t\t\"Welcome, %player.\"\n" +
            "\t],\n" +
            "\t\"LeaveMessages\": [\n" +
            "\t\t\"%player has left us.\",\n" +
            "\t\t\"Bye bye, %player\"\n" +
            "\t]\n" +
            "}";
}
