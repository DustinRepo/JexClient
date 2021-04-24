package me.dustin.jex.load.mixin;

import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.misc.EventScheduleStop;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventHasOutline;
import me.dustin.jex.load.impl.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Session;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient implements IMinecraft {
    @Mutable
    @Shadow
    @Final
    private Session session;

    @Shadow
    private int itemUseCooldown;

    @Shadow
    private int fpsCounter;

    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow @Final private SocialInteractionsManager socialInteractionsManager;

    @Shadow @Final private RenderTickCounter renderTickCounter;

    @Shadow @Final public GameOptions options;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void setRightClickDelayTimer(int timer) {
        this.itemUseCooldown = timer;
    }

    @Override
    public int getFPS() {
        return this.fpsCounter;
    }

    @Override
    public BufferBuilderStorage getBufferBuilderStorage() {
        return this.bufferBuilders;
    }

    @Override
    public RenderTickCounter getRenderTickCounter() {
        return this.renderTickCounter;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        new EventTick().run();
    }

    @Inject(method = "joinWorld", at = @At("HEAD"))
    public void joinWorld(ClientWorld clientWorld, CallbackInfo ci) {
        EventJoinWorld eventJoinWorld = new EventJoinWorld().run();
    }

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    public void openScreen(Screen screen, CallbackInfo cir) {
        EventDisplayScreen eventDisplayScreen = new EventDisplayScreen(screen).run();
        screen = eventDisplayScreen.getScreen();
        if (eventDisplayScreen.isCancelled())
            cir.cancel();

    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    public void hasOutline1(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EventHasOutline eventHasOutline = new EventHasOutline(entity, entity.isGlowing() || this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isPressed() && entity.getType() == EntityType.PLAYER).run();
        cir.setReturnValue(eventHasOutline.isOutline());
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    public void scheduleStop1(CallbackInfo ci) {
        new EventScheduleStop().run();
    }

}
