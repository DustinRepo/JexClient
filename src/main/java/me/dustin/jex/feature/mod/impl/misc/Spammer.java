package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@Feature.Manifest(name = "Spammer", category = Feature.Category.MISC, description = "Spam the chat")
public class Spammer extends Feature {

    private static String baseFileStr =
            "/*\n" +
                    "* Each line of this file will be a new chat message to send to the server.\n" +
                    "* After it reaches the end, the spam will go back to the start.\n" +
                    "* You can also use syntaxes that will be parsed by the game. This is what is currently supported:\n" +
                    "* {$rplayer} - Grab a random player name\n" +
                    "* {$me} - Grab your username\n" +
                    "* {$ri} - Gets a random integer\n" +
                    "* {$rf} - Gets a random decimal number\n" +
                    "* Here are some examples:\n" +
                    "*/\n" +
                    "{$rplayer} has been killed by {$me} with a wooden stick\n" +
                    "{$rplayer}'s coords are x{$ri} z{$ri}\n" +
                    "{$me} is your new god";
    @Op(name = "Source", all = {"Spam.txt", "Jex AdBot", "Toxic"})
    public String source = "Spam.txt";
    @Op(name = "Delay (MS)", max = 30000, inc = 10)
    public int delay = 500;
    private String spamString;
    private Timer timer = new Timer();
    private int currentSpot = 0;
    private String jexAdString =
            "Download Jex Client! https://jexclient.com/\n" +
                    "Thanks to Jex Client, I can use the new dupe!\n" +
                    "What are you fucking stupid? Download Jex Client right now!\n" +
                    "Jex Client: now with Baritone!\n" +
                    "I just found your coords with Jex Client";
    private String toxicString =
            "{$rplayer} is a bitch.\n" +
                    "What the fuck do you want?\n" +
                    "Just imagine your grandma naked\n" +
                    "{$rplayer} has been killed by {$me} with a wooden stick\n" +
                    "Shut the fuck up dude\n" +
                    "{$rplayer}'s coords are x{$ri} z{$ri}\n" +
                    "Oi m8, yu got a loiscence for that sword tha?\n" +
                    "I'll trade 5 rotten flesh to someone for some god gear\n" +
                    "british \"people\"\n" +
                    "{$me} is your new god\n" +
                    "Selling {$rplayer}'s feet pics";

    public static void createSpamFile() {
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(baseFileStr.split("\n")));
        FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), "Spam.txt", arrayList);
    }

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (!timer.hasPassed(delay))
                return;
            String sentence = spamString.split("\n")[currentSpot];
            while (containsSyntax(sentence)) {
                sentence = parseSyntax(sentence);
            }
            NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(sentence));
            timer.reset();
            currentSpot++;
            if (currentSpot > spamString.split("\n").length - 1)
                currentSpot = 0;
        }
    }

    @Override
    public void onEnable() {
        File spamFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Spam.txt");
        if (!spamFile.exists())
            createSpamFile();
        switch (source) {
            case "Spam.txt" -> spamString = readFile();
            case "Jex AdBot" -> spamString = jexAdString;
            case "Toxic" -> spamString = toxicString;
        }
        currentSpot = 0;
        super.onEnable();
    }

    public String readFile() {
        StringBuilder sb = new StringBuilder();
        for (String s : FileHelper.INSTANCE.readFile(ModFileHelper.INSTANCE.getJexDirectory(), "Spam.txt")) {
            if (!s.startsWith("/*") && !s.startsWith("*") && !s.isEmpty()) {
                sb.append(s).append("\n");
            }
        }
        return sb.toString();
    }

    private String parseSyntax(String s) {
        Random random = new Random();
        if (s.contains("{$rplayer}")) {
            int size = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerList().size();
            int r = random.nextInt(size);
            int c = 0;
            for (PlayerListEntry playerListEntry1 : Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerList()) {
                if (c == r) {
                    return s.replace("{$rplayer}", playerListEntry1.getProfile().getName());
                }
                c++;
            }
            return s.replace("{$rplayer}", "someone");
        }
        if (s.contains("{$me}")) {
            return s.replace("{$me}", Wrapper.INSTANCE.getLocalPlayer().getName().asString());
        }
        if (s.contains("{$ri}")) {
            return s.replace("{$ri}", String.valueOf(ClientMathHelper.INSTANCE.randInt(0, 30000000)));
        }
        if (s.contains("{$rf}")) {
            return s.replace("{$rf}", String.valueOf(ClientMathHelper.INSTANCE.randFloat(0, 30000000)));
        }
        return s;
    }

    private boolean containsSyntax(String s) {
        return s.contains("{$rplayer}") || s.contains("{$me}") || s.contains("{$ri}") || s.contains("{$rf}");
    }
}
