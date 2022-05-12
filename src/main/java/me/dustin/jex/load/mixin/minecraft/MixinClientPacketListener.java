package me.dustin.jex.load.mixin.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import me.dustin.jex.event.misc.EventServerTurn;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.event.player.EventPlayerVelocity;
import me.dustin.jex.event.world.EventLoadChunk;
import me.dustin.jex.feature.command.ClientCommandInternals;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.ConnectedServerHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.player.bot.BotClientPlayNetworkHandler;
import me.dustin.jex.load.impl.IClientPacketListener;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener implements IClientPacketListener {

    private float yaw, pitch;
    private EventServerTurn eventServerTurn;

    @Shadow @Final private Connection connection;

    @Shadow public abstract void send(Packet<?> packet);

    @Shadow private ClientLevel level;

    @Shadow private CommandDispatcher<SharedSuggestionProvider> commands;

    @Shadow @Final private ClientSuggestionProvider suggestionsProvider;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void sendPacketPre(Packet<?> packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        EventPacketSent eventPacketSent = new EventPacketSent(packet, EventPacketSent.Mode.PRE).run();
        if (eventPacketSent.isCancelled()) {
            ci.cancel();
        } else if (eventPacketSent.getPacket() != packet) {
            this.connection.send(eventPacketSent.getPacket());
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    public void sendPacketPost(Packet<?> packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        new EventPacketSent(packet, EventPacketSent.Mode.POST).run();
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    public void posLook(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        //fix for viafabric getting stuck on "Loading terrain..." on 2b2t specifically
        if (!Wrapper.INSTANCE.getMinecraft().isLocalServer() && ConnectedServerHelper.INSTANCE.getServerAddress() != null && ConnectedServerHelper.INSTANCE.getServerAddress().getHost().contains("2b2t.org") && Wrapper.INSTANCE.getWorld() != null && Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getMinecraft().setScreen(null);
    }

    @Inject(method = "updateLevelChunk", at = @At("HEAD"), cancellable = true)
    public void loadChunk(int x, int z, ClientboundLevelChunkPacketData chunkData, CallbackInfo ci) {
        LevelChunk worldChunk = level.getChunkSource().replaceWithPacketData(x, z, chunkData.getReadBuffer(), chunkData.getHeightmaps(), chunkData.getBlockEntitiesTagsConsumer(x, z));
        new EventLoadChunk(worldChunk).run();
        ci.cancel();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "handleCommands", at = @At("RETURN"))
    private void onOnCommandTree(ClientboundCommandsPacket packet, CallbackInfo info) {
        if (isBotHandler())
            return;
        ClientCommandInternals.addCommands((CommandDispatcher) commands, (FabricClientCommandSource) suggestionsProvider);
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    public void onPlayerPositionLook1(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            eventServerTurn = new EventServerTurn().run();
            yaw = PlayerHelper.INSTANCE.getYaw();
            pitch = PlayerHelper.INSTANCE.getPitch();
        }

    }

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    public void onPlayerPositionLook2(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        if (eventServerTurn != null && eventServerTurn.isCancelled()) {
            PlayerHelper.INSTANCE.setYaw(yaw);
            PlayerHelper.INSTANCE.setPitch(pitch);
        }
        eventServerTurn = null;
    }

    @Inject(method = "handleExplosion", at = @At("HEAD"), cancellable = true)
    public void onExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener) (Object) this, Wrapper.INSTANCE.getMinecraft());
        Explosion explosion = new Explosion(Wrapper.INSTANCE.getMinecraft().level, (Entity) null, packet.getX(), packet.getY(), packet.getZ(), packet.getPower(), packet.getToBlow());
        explosion.finalizeExplosion(true);
        EventExplosionVelocity eventExplosionVelocity = new EventExplosionVelocity().run();
        if (!eventExplosionVelocity.isCancelled())
            Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement().add((double) packet.getKnockbackX() * eventExplosionVelocity.getMultX(), (double) packet.getKnockbackY() * eventExplosionVelocity.getMultY(), (double) packet.getKnockbackZ() * eventExplosionVelocity.getMultZ()));
        ci.cancel();
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    public void onVelocityUpdate1(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        PacketUtils.ensureRunningOnSameThread(packet, (ClientPacketListener)(Object)this, this.minecraft);
        Entity entity = this.level.getEntity(packet.getId());
        if (entity != null && entity == Wrapper.INSTANCE.getLocalPlayer()) {
            ci.cancel();
            EventPlayerVelocity eventPlayerVelocity = new EventPlayerVelocity(packet.getXa(), packet.getYa(), packet.getZa()).run();
            if (eventPlayerVelocity.isCancelled())
                return;
            entity.lerpMotion((double)eventPlayerVelocity.getVelocityX() / 8000.0D, (double)eventPlayerVelocity.getVelocityY() / 8000.0D, (double)eventPlayerVelocity.getVelocityZ() / 8000.0D);
        }
    }

    @Override
    public void setWorld(ClientLevel world) {
        this.level = world;
    }

    private boolean isBotHandler() {
        ClientPacketListener me = (ClientPacketListener)(Object)this;
        return me instanceof BotClientPlayNetworkHandler;
    }
}
