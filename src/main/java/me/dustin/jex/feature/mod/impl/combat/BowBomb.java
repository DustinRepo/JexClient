package me.dustin.jex.feature.mod.impl.combat;

import com.google.gson.JsonArray;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventStopUsingItem;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.item.Items;
import me.dustin.events.core.annotate.EventPointer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Bow Exploit")
public class BowBomb extends Feature {

    @Op(name = "Amount", max = 1000, inc = 10)
    public int amount = 100;

    @EventPointer
    private final EventListener<EventStopUsingItem> eventStopUsingItem = new EventListener<>(event -> {
        LocalPlayer player = Wrapper.INSTANCE.getLocalPlayer();
        if (player.getMainHandItem().getItem().equals(Items.BOW)) {
            player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
            for (int i = 0; i < amount; ++i) {
                player.connection.send(new ServerboundMovePlayerPacket.Pos(player.getX(), player.getY() - 1.0E-9, player.getZ(), true));
                player.connection.send(new ServerboundMovePlayerPacket.Pos(player.getX(), player.getY() + 1.0E-9, player.getZ(), false));
            }
        }
    });
}
