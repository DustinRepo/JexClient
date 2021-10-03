package me.dustin.jex.load.mixin.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import me.dustin.jex.event.misc.EventServerTurn;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.event.player.EventPlayerVelocity;
import me.dustin.jex.feature.command.ClientCommandInternals;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Shadow private ClientWorld world;
    @Shadow @Final private MinecraftClient client;
    private float yaw, pitch;
    private EventServerTurn eventServerTurn;

    @Shadow
    private CommandDispatcher<CommandSource> commandDispatcher;

    @Shadow
    @Final
    private ClientCommandSource commandSource;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "onCommandTree", at = @At("RETURN"))
    private void onOnCommandTree(CommandTreeS2CPacket packet, CallbackInfo info) {
        ClientCommandInternals.addCommands((CommandDispatcher) commandDispatcher, (FabricClientCommandSource) commandSource);
    }

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    public void onPlayerPositionLook1(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            eventServerTurn = new EventServerTurn().run();
            yaw = PlayerHelper.INSTANCE.getYaw();
            pitch = PlayerHelper.INSTANCE.getPitch();
        }

    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    public void onPlayerPositionLook2(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (eventServerTurn != null && eventServerTurn.isCancelled()) {
            PlayerHelper.INSTANCE.setYaw(yaw);
            PlayerHelper.INSTANCE.setPitch(pitch);
        }
        eventServerTurn = null;
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    public void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, Wrapper.INSTANCE.getMinecraft());
        Explosion explosion = new Explosion(Wrapper.INSTANCE.getMinecraft().world, (Entity) null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks());
        explosion.affectWorld(true);
        EventExplosionVelocity eventExplosionVelocity = new EventExplosionVelocity().run();
        if (!eventExplosionVelocity.isCancelled())
            Wrapper.INSTANCE.getLocalPlayer().setVelocity(Wrapper.INSTANCE.getLocalPlayer().getVelocity().add((double) packet.getPlayerVelocityX() * eventExplosionVelocity.getMultX(), (double) packet.getPlayerVelocityY() * eventExplosionVelocity.getMultY(), (double) packet.getPlayerVelocityZ() * eventExplosionVelocity.getMultZ()));
        ci.cancel();
    }

    @Inject(method = "onVelocityUpdate", at = @At("HEAD"), cancellable = true)
    public void onVelocityUpdate1(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
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

}
