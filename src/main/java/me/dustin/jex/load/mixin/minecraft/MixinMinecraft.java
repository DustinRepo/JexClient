package me.dustin.jex.load.mixin.minecraft;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import me.dustin.jex.event.misc.*;
import me.dustin.jex.event.render.EventHasOutline;
import me.dustin.jex.feature.command.ClientCommandInternals;
import me.dustin.jex.load.impl.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
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
public abstract class MixinMinecraft implements IMinecraft {
    @Mutable
    @Shadow
    @Final
    private Session session;

    @Shadow @Final public GameOptions options;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Mutable
    @Shadow @Final private MinecraftSessionService sessionService;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow private int itemUseCooldown;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void setRightClickDelayTimer(int timer) {
        this.itemUseCooldown = timer;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickPre(CallbackInfo ci) {
        new EventTick(EventTick.Mode.PRE).run();
    }
    @Inject(method = "tick", at = @At("RETURN"))
    public void tickPost(CallbackInfo ci) {
        new EventTick(EventTick.Mode.POST).run();
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    public void joinWorld(ClientWorld clientWorld, CallbackInfo ci) {
        new EventSetLevel().run();
    }

    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    public void framerateLimit(CallbackInfoReturnable<Integer> cir) {
        EventGetFramerateLimit eventGetFramerateLimit = new EventGetFramerateLimit().run();
        if (eventGetFramerateLimit.getLimit() != -1)
            cir.setReturnValue(eventGetFramerateLimit.getLimit());
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void openScreen(Screen screen, CallbackInfo cir) {
        EventSetScreen eventSetScreen = new EventSetScreen(screen).run();
        if (eventSetScreen.isCancelled()) {
            cir.cancel();
            if (eventSetScreen.getScreen() != screen)
                setScreen(eventSetScreen.getScreen());
        }

    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    public void hasOutline1(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EventHasOutline eventHasOutline = new EventHasOutline(entity, entity.isGlowing() || this.player != null && this.player.isSpectator() && this.options.spectatorOutlinesKey.isPressed() && entity.getType() == EntityType.PLAYER).run();
        cir.setReturnValue(eventHasOutline.isOutline());
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    public void scheduleStop1(CallbackInfo ci) {
        new EventStop().run();
    }

    @Override
    public void setSessionService(MinecraftSessionService sessionService) {
        this.sessionService = sessionService;
    }
}
