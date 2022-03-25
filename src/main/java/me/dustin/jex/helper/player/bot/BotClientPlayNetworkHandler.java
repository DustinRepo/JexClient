package me.dustin.jex.helper.player.bot;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.load.impl.IChatHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.util.telemetry.TelemetrySender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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
        super.onGameJoin(packet);
        playerBot.setPlayer(new OtherClientPlayerEntity(this.getWorld(), playerBot.getGameProfile()));
        playerBot.setPlayerInventory(new PlayerInventory(playerBot.getPlayer()));
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
            playerBot.setPlayer(new OtherClientPlayerEntity(this.getWorld(), playerBot.getGameProfile()));
            playerBot.setPlayerInventory(new PlayerInventory(playerBot.getPlayer()));
        }
    }

    @Override
    public void onEntityPosition(EntityPositionS2CPacket packet) {
        if (packet.getId() == entityId) {
            super.onEntityPosition(packet);
            if (playerBot.getPlayer() != null) {
                playerBot.getPlayer().setPos(packet.getX(), packet.getY(), packet.getZ());
            }
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
    public void onGameMessage(GameMessageS2CPacket packet) {
        IChatHud iChatHud = (IChatHud) Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud();
        try {
            if (!iChatHud.containsMessage(packet.getMessage().getString()))
                ChatHelper.INSTANCE.addRawMessage(String.format("%s[%s%s%s]%s: %s%s", Formatting.DARK_GRAY, Formatting.GREEN, playerBot.getGameProfile().getName(), Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY, packet.getMessage().getString()));
        } catch (Exception e){}
    }

    @Override
    public void onUnloadChunk(UnloadChunkS2CPacket packet) {
        ChunkPos chunkPos = new ChunkPos(packet.getX(), packet.getZ());
        ChunkPos mePos = Wrapper.INSTANCE.getLocalPlayer().getChunkPos();
        float x = chunkPos.x - mePos.x;
        float z = chunkPos.z - mePos.z;
        float distance = x * x + z * z;
        if (distance <= Wrapper.INSTANCE.getOptions().getViewDistance())
            return;
        super.onUnloadChunk(packet);
    }

    @Override
    public void onInventory(InventoryS2CPacket packet) {
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
}
