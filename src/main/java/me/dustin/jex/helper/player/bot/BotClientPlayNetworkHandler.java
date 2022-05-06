package me.dustin.jex.helper.player.bot;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IChatHud;
import me.dustin.jex.load.impl.IClientPlayNetworkHandler;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.*;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.util.telemetry.TelemetrySender;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.*;

public class BotClientPlayNetworkHandler extends ClientPlayNetworkHandler {
    private int entityId;
    private final PlayerBot playerBot;
    public BotClientPlayNetworkHandler(MinecraftClient client, Screen screen, ClientConnection connection, GameProfile profile, TelemetrySender telemetrySender, PlayerBot playerBot) {
        super(client, screen, connection, profile, telemetrySender);
        this.playerBot = playerBot;
    }

    @Override
    public void onGameJoin(GameJoinS2CPacket packet) {
        entityId = packet.playerEntityId();

        ClientWorld.Properties properties;
        NetworkThreadUtils.forceMainThread(packet, this, Wrapper.INSTANCE.getMinecraft());
        //this.client.interactionManager = new ClientPlayerInteractionManager(this.client, this);
        //this.registryManager = packet.registryManager();
        if (!this.playerBot.getClientConnection().isLocal()) {
            //this.registryManager.streamAllRegistries().forEach(entry -> entry.value().clearTags());
        }
        ArrayList<RegistryKey<World>> list = Lists.newArrayList(packet.dimensionIds());
        Collections.shuffle(list);
        RegistryKey<World> registryKey = packet.dimensionId();
        RegistryEntry<DimensionType> registryEntry = packet.dimensionType();
        int chunkLoadDistance = packet.viewDistance();
        int simulationDistance = packet.simulationDistance();
        boolean bl = packet.debugWorld();
        boolean bl2 = packet.flatWorld();
        properties = new ClientWorld.Properties(Difficulty.NORMAL, packet.hardcore(), bl2);
        playerBot.setWorld(new ClientWorld(this, properties, registryKey, registryEntry, chunkLoadDistance, simulationDistance, Wrapper.INSTANCE.getMinecraft()::getProfiler, Wrapper.INSTANCE.getWorldRenderer(), bl, packet.sha256Seed()));

        IClientPlayNetworkHandler iClientPlayNetworkHandler = (IClientPlayNetworkHandler)this;
        iClientPlayNetworkHandler.setWorld(playerBot.getWorld());

        playerBot.setPlayer(new OtherClientPlayerEntity(this.getWorld(), playerBot.getGameProfile(), playerBot.getKeyPair().publicKey()));
        if (playerBot.getPlayerInventory() == null)
            playerBot.setPlayerInventory(new PlayerInventory(playerBot.getPlayer()));
        playerBot.getPlayer().world = playerBot.getWorld();

        int i = packet.playerEntityId();
        this.playerBot.getPlayer().setId(i);
        this.playerBot.getWorld().addPlayer(i, (AbstractClientPlayerEntity) this.playerBot.getPlayer());
        this.playerBot.getClientConnection().send(new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, new PacketByteBuf(Unpooled.buffer()).writeString(ClientBrandRetriever.getClientModName())));
        sendClientSettings();
    }

    public void sendClientSettings() {
        if (this.playerBot.getPlayer() != null) {
            int i = 0;
            for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
                if (Wrapper.INSTANCE.getOptions().isPlayerModelPartEnabled(playerModelPart))
                    i |= playerModelPart.getBitFlag();
            }
            this.playerBot.getClientConnection().send(new ClientSettingsC2SPacket(Wrapper.INSTANCE.getOptions().language, Wrapper.INSTANCE.getOptions().getViewDistance().getValue(), Wrapper.INSTANCE.getOptions().getChatVisibility().getValue(), Wrapper.INSTANCE.getOptions().getChatColors().getValue(), i, Wrapper.INSTANCE.getOptions().getMainArm().getValue(), Wrapper.INSTANCE.getMinecraft().shouldFilterText(), Wrapper.INSTANCE.getOptions().getAllowServerListing().getValue()));
        }
    }

    @Override
    public void onPlayerPositionLook(PlayerPositionLookS2CPacket packet) {
        this.getConnection().send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
        if (playerBot.getPlayer() != null) {
            double i;
            double h;
            double g;
            double f;
            double e;
            double d;
            NetworkThreadUtils.forceMainThread(packet, this, Wrapper.INSTANCE.getMinecraft());
            PlayerEntity playerEntity = playerBot.getPlayer();
            if (packet.shouldDismount()) {
                ((PlayerEntity)playerEntity).dismountVehicle();
            }
            Vec3d vec3d = playerEntity.getVelocity();
            boolean bl = packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.X);
            boolean bl2 = packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.Y);
            boolean bl3 = packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.Z);
            if (bl) {
                d = vec3d.getX();
                e = playerEntity.getX() + packet.getX();
                playerEntity.lastRenderX += packet.getX();
            } else {
                d = 0.0;
                playerEntity.lastRenderX = e = packet.getX();
            }
            if (bl2) {
                f = vec3d.getY();
                g = playerEntity.getY() + packet.getY();
                playerEntity.lastRenderY += packet.getY();
            } else {
                f = 0.0;
                playerEntity.lastRenderY = g = packet.getY();
            }
            if (bl3) {
                h = vec3d.getZ();
                i = playerEntity.getZ() + packet.getZ();
                playerEntity.lastRenderZ += packet.getZ();
            } else {
                h = 0.0;
                playerEntity.lastRenderZ = i = packet.getZ();
            }
            playerEntity.setPos(e, g, i);
            playerEntity.prevX = e;
            playerEntity.prevY = g;
            playerEntity.prevZ = i;
            playerEntity.setVelocity(d, f, h);
            float j = packet.getYaw();
            float k = packet.getPitch();
            if (packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.X_ROT)) {
                k += playerEntity.getPitch();
            }
            if (packet.getFlags().contains((Object)PlayerPositionLookS2CPacket.Flag.Y_ROT)) {
                j += playerEntity.getYaw();
            }
            playerEntity.updatePositionAndAngles(e, g, i, j, k);
            getConnection().send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch(), false));
            playerBot.setRotation(new Vec3d(packet.getYaw(), packet.getPitch(), 0));
            playerBot.getPlayer().setPos(packet.getX(), packet.getY(), packet.getZ());
        }
    }

    @Override
    public void onDeathMessage(DeathMessageS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, Wrapper.INSTANCE.getMinecraft());
        if (packet.getEntityId() == entityId) {
            sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
            playerBot.setPlayer(new OtherClientPlayerEntity(this.getWorld(), playerBot.getGameProfile(), playerBot.getKeyPair().publicKey()));
            playerBot.setPlayerInventory(new PlayerInventory(playerBot.getPlayer()));
        }
    }

    @Override
    public void onDisconnect(DisconnectS2CPacket packet) {
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " disconnected for reason: " + Formatting.RED + packet.getReason().getString());
        playerBot.setPlayer(null);
        playerBot.setPlayerInventory(null);
        playerBot.setConnected(false);
        PlayerBot.getPlayerBots().remove(playerBot);
        super.onDisconnect(packet);
    }

    @Override
    public void onChatMessage(ChatMessageS2CPacket packet) {
        IChatHud iChatHud = (IChatHud) Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud();
        try {
            if (!iChatHud.containsMessage(packet.content().getString()))
                ChatHelper.INSTANCE.addRawMessage(String.format("%s[%s%s%s]%s: %s%s", Formatting.DARK_GRAY, Formatting.GREEN, playerBot.getGameProfile().getName(), Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY, packet.content().getString()));
        } catch (Exception e){}
    }

    @Override
    public void onInventory(InventoryS2CPacket packet) {
        if (playerBot.getPlayerInventory() == null)
            playerBot.setPlayerInventory(new PlayerInventory(playerBot.getPlayer()));
        for (int i = 0; i < packet.getContents().size(); i++) {
            ItemStack stack = packet.getContents().get(i);
            playerBot.getPlayerInventory().setStack(i, stack);
        }
    }

    @Override
    public void onCooldownUpdate(CooldownUpdateS2CPacket packet) {
    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotS2CPacket packet) {
    }

    @Override
    public void onPlayerActionResponse(PlayerActionResponseS2CPacket packet) {
    }

    @Override
    public void onPlayerAbilities(PlayerAbilitiesS2CPacket packet) {
    }

    @Override
    public void onLightUpdate(LightUpdateS2CPacket packet) {
    }

    @Override
    public void onExperienceBarUpdate(ExperienceBarUpdateS2CPacket packet) {
    }

    @Override
    public void onOpenHorseScreen(OpenHorseScreenS2CPacket packet) {
    }

    @Override
    public void onOpenScreen(OpenScreenS2CPacket packet) {
    }

    @Override
    public void onOpenWrittenBook(OpenWrittenBookS2CPacket packet) {
    }

    @Override
    public void onSignEditorOpen(SignEditorOpenS2CPacket packet) {
    }

    @Override
    public void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {
    }

    @Override
    public void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet) {
    }

    @Override
    public void onPlayerSpawn(PlayerSpawnS2CPacket packet) {
    }

    @Override
    public void onPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet) {
    }

    @Override
    public void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet) {
    }

    @Override
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet) {
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet) {
    }

    @Override
    public void onHealthUpdate(HealthUpdateS2CPacket packet) {
    }

    @Override
    public void onEntitySetHeadYaw(EntitySetHeadYawS2CPacket packet) {
    }
}
