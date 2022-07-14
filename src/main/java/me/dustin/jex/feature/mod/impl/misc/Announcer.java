package me.dustin.jex.feature.mod.impl.misc;

import java.io.File;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;

public class Announcer extends Feature {

    public final Property<Long> messageDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Message Delay")
            .value(1000L)
            .min(50)
            .max(5000)
            .inc(10)
            .build();

    private final File announcerFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "announcer.json");

    private final ArrayList<String> joinMessages = new ArrayList<>();
    private final ArrayList<String> leaveMessages = new ArrayList<>();

    private final StopWatch stopWatch = new StopWatch();

    public Announcer() {
        super(Category.MISC, "Fastest way to get muted! Fully customizable with files in the Jex folder");
    }

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getLocalPlayer().age < 30 || !stopWatch.hasPassed(messageDelayProperty.value()))
            return;
        PlayerListS2CPacket playerListPacket = (PlayerListS2CPacket) event.getPacket();

        if (playerListPacket.getAction() == PlayerListS2CPacket.Action.REMOVE_PLAYER) {
            Entry entry = playerListPacket.getEntries().get(0);
            if (entry != null) {
                String name = entry.getProfile().getName();
                int rand = ClientMathHelper.INSTANCE.getRandom(leaveMessages.size());
                String message = leaveMessages.get(rand).replace("%player", name);
                if (message.startsWith("/"))
                    Wrapper.INSTANCE.getLocalPlayer().sendCommand(message.substring(1));
                else
                    ChatHelper.INSTANCE.sendChatMessage(message);
                stopWatch.reset();
            }
        } else if (playerListPacket.getAction() == PlayerListS2CPacket.Action.ADD_PLAYER) {
            Entry entry = playerListPacket.getEntries().get(0);
            if (entry != null) {
                String name = entry.getProfile().getName();
                int rand = ClientMathHelper.INSTANCE.getRandom(joinMessages.size());
                String message = joinMessages.get(rand).replace("%player", name);
                if (message.startsWith("/"))
                    Wrapper.INSTANCE.getLocalPlayer().sendCommand(message.substring(1));
                else
                    ChatHelper.INSTANCE.sendChatMessage(message);
                stopWatch.reset();
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, PlayerListS2CPacket.class));

    @Override
    public void onEnable() {
        loadFiles();
        super.onEnable();
    }

    public void loadFiles() {
     if (!announcerFile.exists())
         createDefaultAnnouncerFile();
     try {
         JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(FileHelper.INSTANCE.readFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "announcer.json")), JsonObject.class);
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
        FileHelper.INSTANCE.writeFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "announcer.json"), arrayList);
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
