package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.PlayerListEntry;
import me.dustin.jex.feature.mod.core.Feature;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Spammer extends Feature {

    public final Property<SpamSource> sourceProperty = new Property.PropertyBuilder<SpamSource>(this.getClass())
            .name("Source")
            .description("The source for the list of messages to spam.")
            .value(SpamSource.SPAM_FILE)
            .build();
    public final Property<String> delayProperty = new Property.PropertyBuilder<String>(this.getClass())
            .name("Delay (MS)")
            .value("500L")
            .max(5)
            .build();

    private String spamString;
    private int currentSpot = 0;
    private final StopWatch stopWatch = new StopWatch();

    public Spammer() {
        super(Category.MISC, "Spam the chat");
    }

    public static void createSpamFile() {
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(baseFileStr.split("\n")));
        FileHelper.INSTANCE.writeFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "Spam.txt"), arrayList);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        long delay = Long.valueOf(delayProperty.value());
        if (!stopWatch.hasPassed(delay))
            return;
        String sentence = spamString.split("\n")[currentSpot];
        while (containsSyntax(sentence)) {
            sentence = parseSyntax(sentence);
        }
        if (sentence.startsWith("/"))
            ChatHelper.INSTANCE.sendCommand(sentence.substring(1));
        else
            ChatHelper.INSTANCE.sendChatMessage(sentence);
        stopWatch.reset();
        currentSpot++;
        if (currentSpot > spamString.split("\n").length - 1)
            currentSpot = 0;
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onEnable() {
        File spamFile = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Spam.txt");
        if (!spamFile.exists())
            createSpamFile();
        switch (sourceProperty.value()) {
            case SPAM_FILE -> spamString = readFile();
            case JEX_ADBOT -> spamString = jexAdString;
            case TOXIC -> spamString = toxicString;
        }
        currentSpot = 0;
        super.onEnable();
    }

    public String readFile() {
        StringBuilder sb = new StringBuilder();
        for (String s : FileHelper.INSTANCE.readFile(new File(ModFileHelper.INSTANCE.getJexDirectory(), "Spam.txt")).split("\n")) {
            if (!s.startsWith("/*") && !s.startsWith("*") && !s.isEmpty()) {
                sb.append(s).append("\n");
            }
        }
        return sb.toString();
    }

    private String parseSyntax(String s) {
        Random random = new Random();
        if (s.contains("{$rplayer}")) {
            int size = Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerList().size();
            int r = random.nextInt(size);
            int c = 0;
            for (PlayerListEntry playerListEntry1 : Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerList()) {
                if (c == r) {
                    return s.replace("{$rplayer}", playerListEntry1.getProfile().getName());
                }
                c++;
            }
            return s.replace("{$rplayer}", "someone");
        }
        if (s.contains("{$me}")) {
            return s.replace("{$me}", Wrapper.INSTANCE.getLocalPlayer().getName().getString());
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

    private String jexAdString =
            "Download Jex Client! https://discord.gg/BUcUGu6gfA\n" +
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

    public enum SpamSource {
        SPAM_FILE, JEX_ADBOT, TOXIC
    }
}
