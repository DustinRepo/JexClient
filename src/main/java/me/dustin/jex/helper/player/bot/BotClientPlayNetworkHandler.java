package me.dustin.jex.helper.player.bot;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IChatComponent;
import me.dustin.jex.load.impl.IClientPacketListener;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class BotClientPlayNetworkHandler extends ClientPacketListener {
    private int entityId;
    private final PlayerBot playerBot;
    public BotClientPlayNetworkHandler(Minecraft client, Screen screen, Connection connection, GameProfile profile, ClientTelemetryManager telemetrySender, PlayerBot playerBot) {
        super(client, screen, connection, profile, telemetrySender);
        this.playerBot = playerBot;
    }

    @Override
    public void handleLogin(ClientboundLoginPacket packet) {
        entityId = packet.playerId();

        ClientLevel.ClientLevelData properties;
        PacketUtils.ensureRunningOnSameThread(packet, this, Wrapper.INSTANCE.getMinecraft());
        //this.client.interactionManager = new ClientPlayerInteractionManager(this.client, this);
        //this.registryManager = packet.registryManager();
        if (!this.playerBot.getClientConnection().isMemoryConnection()) {
            //this.registryManager.streamAllRegistries().forEach(entry -> entry.value().clearTags());
        }
        ArrayList<ResourceKey<Level>> list = Lists.newArrayList(packet.levels());
        Collections.shuffle(list);
        ResourceKey<Level> registryKey = packet.dimension();
        Holder<DimensionType> registryEntry = packet.dimensionType();
        int chunkLoadDistance = packet.chunkRadius();
        int simulationDistance = packet.simulationDistance();
        boolean bl = packet.isDebug();
        boolean bl2 = packet.isFlat();
        properties = new ClientLevel.ClientLevelData(Difficulty.NORMAL, packet.hardcore(), bl2);
        playerBot.setWorld(new ClientLevel(this, properties, registryKey, registryEntry, chunkLoadDistance, simulationDistance, Wrapper.INSTANCE.getMinecraft()::getProfiler, Wrapper.INSTANCE.getWorldRenderer(), bl, packet.seed()));

        IClientPacketListener iClientPacketListener = (IClientPacketListener)this;
        iClientPacketListener.setWorld(playerBot.getWorld());

        playerBot.setPlayer(new RemotePlayer(this.getLevel(), playerBot.getGameProfile(), playerBot.getKeyPair().publicKey()));
        if (playerBot.getPlayerInventory() == null)
            playerBot.setPlayerInventory(new Inventory(playerBot.getPlayer()));
        playerBot.getPlayer().level = playerBot.getWorld();

        int i = packet.playerId();
        this.playerBot.getPlayer().setId(i);
        this.playerBot.getWorld().addPlayer(i, (AbstractClientPlayer) this.playerBot.getPlayer());
        this.playerBot.getClientConnection().send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(ClientBrandRetriever.getClientModName())));
        sendClientSettings();
    }

    public void sendClientSettings() {
        if (this.playerBot.getPlayer() != null) {
            int i = 0;
            for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
                if (Wrapper.INSTANCE.getOptions().isModelPartEnabled(playerModelPart))
                    i |= playerModelPart.getMask();
            }
            this.playerBot.getClientConnection().send(new ServerboundClientInformationPacket(Wrapper.INSTANCE.getOptions().languageCode, Wrapper.INSTANCE.getOptions().renderDistance().get(), Wrapper.INSTANCE.getOptions().chatVisibility().get(), Wrapper.INSTANCE.getOptions().chatColors().get(), i, Wrapper.INSTANCE.getOptions().mainHand().get(), Wrapper.INSTANCE.getMinecraft().isTextFilteringEnabled(), Wrapper.INSTANCE.getOptions().allowServerListing().get()));
        }
    }

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket packet) {
        this.getConnection().send(new ServerboundAcceptTeleportationPacket(packet.getId()));
        if (playerBot.getPlayer() != null) {
            double i;
            double h;
            double g;
            double f;
            double e;
            double d;
            PacketUtils.ensureRunningOnSameThread(packet, this, Wrapper.INSTANCE.getMinecraft());
            Player playerEntity = playerBot.getPlayer();
            if (packet.requestDismountVehicle()) {
                ((Player)playerEntity).removeVehicle();
            }
            Vec3 vec3d = playerEntity.getDeltaMovement();
            boolean bl = packet.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.X);
            boolean bl2 = packet.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.Y);
            boolean bl3 = packet.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.Z);
            if (bl) {
                d = vec3d.x();
                e = playerEntity.getX() + packet.getX();
                playerEntity.xOld += packet.getX();
            } else {
                d = 0.0;
                playerEntity.xOld = e = packet.getX();
            }
            if (bl2) {
                f = vec3d.y();
                g = playerEntity.getY() + packet.getY();
                playerEntity.yOld += packet.getY();
            } else {
                f = 0.0;
                playerEntity.yOld = g = packet.getY();
            }
            if (bl3) {
                h = vec3d.z();
                i = playerEntity.getZ() + packet.getZ();
                playerEntity.zOld += packet.getZ();
            } else {
                h = 0.0;
                playerEntity.zOld = i = packet.getZ();
            }
            playerEntity.setPosRaw(e, g, i);
            playerEntity.xo = e;
            playerEntity.yo = g;
            playerEntity.zo = i;
            playerEntity.setDeltaMovement(d, f, h);
            float j = packet.getYRot();
            float k = packet.getXRot();
            if (packet.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.X_ROT)) {
                k += playerEntity.getXRot();
            }
            if (packet.getRelativeArguments().contains((Object)ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT)) {
                j += playerEntity.getYRot();
            }
            playerEntity.absMoveTo(e, g, i, j, k);
            getConnection().send(new ServerboundMovePlayerPacket.PosRot(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYRot(), playerEntity.getXRot(), false));
            playerBot.setRotation(new Vec3(packet.getYRot(), packet.getXRot(), 0));
            playerBot.getPlayer().setPosRaw(packet.getX(), packet.getY(), packet.getZ());
        }
    }

    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, Wrapper.INSTANCE.getMinecraft());
        if (packet.getPlayerId() == entityId) {
            send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
            playerBot.setPlayer(new RemotePlayer(this.getLevel(), playerBot.getGameProfile(), playerBot.getKeyPair().publicKey()));
            playerBot.setPlayerInventory(new Inventory(playerBot.getPlayer()));
        }
    }

    @Override
    public void handleDisconnect(ClientboundDisconnectPacket packet) {
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " disconnected for reason: " + ChatFormatting.RED + packet.getReason().getString());
        playerBot.setPlayer(null);
        playerBot.setPlayerInventory(null);
        playerBot.setConnected(false);
        PlayerBot.getPlayerBots().remove(playerBot);
        super.handleDisconnect(packet);
    }

    @Override
    public void handlePlayerChat(ClientboundPlayerChatPacket packet) {
        IChatComponent iChatComponent = (IChatComponent) Wrapper.INSTANCE.getMinecraft().gui.getChat();
        try {
            if (!iChatComponent.containsMessage(packet.content().getString()))
                ChatHelper.INSTANCE.addRawMessage(String.format("%s[%s%s%s]%s: %s%s", ChatFormatting.DARK_GRAY, ChatFormatting.GREEN, playerBot.getGameProfile().getName(), ChatFormatting.DARK_GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY, packet.content().getString()));
        } catch (Exception e){}
    }

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket packet) {
        if (playerBot.getPlayerInventory() == null)
            playerBot.setPlayerInventory(new Inventory(playerBot.getPlayer()));
        for (int i = 0; i < packet.getItems().size(); i++) {
            ItemStack stack = packet.getItems().get(i);
            playerBot.getPlayerInventory().setItem(i, stack);
        }
    }

    @Override
    public void handleItemCooldown(ClientboundCooldownPacket packet) {
    }

    @Override
    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket packet) {
    }

    @Override
    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket packet) {
    }

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket packet) {
    }

    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket packet) {
    }

    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket packet) {
    }

    @Override
    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket packet) {
    }

    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket packet) {
    }

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket packet) {
    }

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket packet) {
    }

    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet) {
    }

    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket packet) {
    }

    @Override
    public void handleAddPlayer(ClientboundAddPlayerPacket packet) {
    }

    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket packet) {
    }

    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket packet) {
    }

    @Override
    public void handleSetTime(ClientboundSetTimePacket packet) {
    }

    @Override
    public void handleRespawn(ClientboundRespawnPacket packet) {
    }

    @Override
    public void handleSetHealth(ClientboundSetHealthPacket packet) {
    }

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket packet) {
    }
}
