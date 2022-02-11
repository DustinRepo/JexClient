package me.dustin.jex.load.mixin.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import me.dustin.jex.event.misc.EventServerTurn;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.event.player.EventPlayerVelocity;
import me.dustin.jex.feature.command.ClientCommandInternals;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.player.bot.BotClientPlayNetworkHandler;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow private ClientWorld world;
    @Shadow @Final private MinecraftClient client;
    private float yaw, pitch;
    private EventServerTurn eventServerTurn;

    @Shadow
    private CommandDispatcher<CommandSource> commandDispatcher;

    @Shadow
    @Final
    private ClientCommandSource commandSource;

    @Shadow @Final private ClientConnection connection;

    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
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

    @Inject(method = "sendPacket", at = @At("HEAD"))
    public void sendPacketPost(Packet<?> packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        EventPacketSent eventPacketSent = new EventPacketSent(packet, EventPacketSent.Mode.POST).run();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "onCommandTree", at = @At("RETURN"))
    private void onOnCommandTree(CommandTreeS2CPacket packet, CallbackInfo info) {
        if (isBotHandler())
            return;
        ClientCommandInternals.addCommands((CommandDispatcher) commandDispatcher, (FabricClientCommandSource) commandSource);
    }

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    public void onPlayerPositionLook1(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            eventServerTurn = new EventServerTurn().run();
            yaw = PlayerHelper.INSTANCE.getYaw();
            pitch = PlayerHelper.INSTANCE.getPitch();
        }

    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    public void onPlayerPositionLook2(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        if (eventServerTurn != null && eventServerTurn.isCancelled()) {
            PlayerHelper.INSTANCE.setYaw(yaw);
            PlayerHelper.INSTANCE.setPitch(pitch);
        }
        eventServerTurn = null;
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    public void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, Wrapper.INSTANCE.getMinecraft());
        Explosion explosion = new Explosion(Wrapper.INSTANCE.getMinecraft().world, (Entity) null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks());
        explosion.affectWorld(true);
        EventExplosionVelocity eventExplosionVelocity = new EventExplosionVelocity().run();
        if (!eventExplosionVelocity.isCancelled())
            Wrapper.INSTANCE.getLocalPlayer().setVelocity(Wrapper.INSTANCE.getLocalPlayer().getVelocity().add((double) packet.getPlayerVelocityX() * eventExplosionVelocity.getMultX(), (double) packet.getPlayerVelocityY() * eventExplosionVelocity.getMultY(), (double) packet.getPlayerVelocityZ() * eventExplosionVelocity.getMultZ()));
        ci.cancel();
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    public void onVelocityUpdate1(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (isBotHandler())
            return;
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler)(Object)this, this.client);
        Entity entity = this.world.getEntityById(packet.getId());
        if (entity != null && entity == Wrapper.INSTANCE.getLocalPlayer()) {
            ci.cancel();
            EventPlayerVelocity eventPlayerVelocity = new EventPlayerVelocity(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ()).run();
            if (eventPlayerVelocity.isCancelled())
                return;
            entity.setVelocityClient((double)eventPlayerVelocity.getVelocityX() / 8000.0D, (double)eventPlayerVelocity.getVelocityY() / 8000.0D, (double)eventPlayerVelocity.getVelocityZ() / 8000.0D);
        }
    }

    //bot stuff
    @Redirect(method = "onGameJoin", at = @At(value = "NEW", target = "net/minecraft/client/network/ClientPlayerInteractionManager"))
    public ClientPlayerInteractionManager newInteractionManager(MinecraftClient client, ClientPlayNetworkHandler networkHandler) {
        if (isBotHandler()) {
            return Wrapper.INSTANCE.getInteractionManager();
        }
        return new ClientPlayerInteractionManager(client, networkHandler);
    }

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE", target = "net/minecraft/client/MinecraftClient.joinWorld (Lnet/minecraft/client/world/ClientWorld;)V"), cancellable = true)
    public void onJoinBot(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (isBotHandler()) {
            ci.cancel();
            sendClientSettings();
            this.connection.send(new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, new PacketByteBuf(Unpooled.buffer()).writeString(ClientBrandRetriever.getClientModName())));
        }
    }

    public void sendClientSettings() {
        if (this.client.player != null) {
            int i = 0;
            for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
                if (Wrapper.INSTANCE.getOptions().isPlayerModelPartEnabled(playerModelPart))
                    i |= playerModelPart.getBitFlag();
            }
            this.sendPacket(new ClientSettingsC2SPacket(Wrapper.INSTANCE.getOptions().language, Wrapper.INSTANCE.getOptions().viewDistance, Wrapper.INSTANCE.getOptions().chatVisibility, Wrapper.INSTANCE.getOptions().chatColors, i, Wrapper.INSTANCE.getOptions().mainArm, this.client.shouldFilterText(), Wrapper.INSTANCE.getOptions().allowServerListing));
        }
    }

    private boolean isBotHandler() {
        ClientPlayNetworkHandler me = (ClientPlayNetworkHandler)(Object)this;
        return me instanceof BotClientPlayNetworkHandler;
    }
}
