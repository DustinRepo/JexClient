package me.dustin.jex.load.mixin.minecraft;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import me.dustin.jex.event.misc.*;
import me.dustin.jex.event.render.EventHasOutline;
import me.dustin.jex.feature.command.ClientCommandInternals;
import me.dustin.jex.load.impl.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraft {
    @Mutable
    @Shadow
    @Final
    private User user;

    @Shadow @Final public Options options;

    @Shadow @Nullable public LocalPlayer player;

    @Mutable
    @Shadow @Final private MinecraftSessionService minecraftSessionService;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow private int rightClickDelay;

    @Override
    public void setSession(User session) {
        this.user = session;
    }

    @Override
    public void setRightClickDelayTimer(int timer) {
        this.rightClickDelay = timer;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(GameConfig args, CallbackInfo info) {
        ClientCommandInternals.finalizeInit();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickPre(CallbackInfo ci) {
        new EventTick(EventTick.Mode.PRE).run();
    }
    @Inject(method = "tick", at = @At("RETURN"))
    public void tickPost(CallbackInfo ci) {
        new EventTick(EventTick.Mode.POST).run();
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    public void joinWorld(ClientLevel clientWorld, CallbackInfo ci) {
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

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    public void hasOutline1(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EventHasOutline eventHasOutline = new EventHasOutline(entity, entity.isCurrentlyGlowing() || this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isDown() && entity.getType() == EntityType.PLAYER).run();
        cir.setReturnValue(eventHasOutline.isOutline());
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void scheduleStop1(CallbackInfo ci) {
        new EventStop().run();
    }

    @Override
    public void setSessionService(MinecraftSessionService sessionService) {
        this.minecraftSessionService = sessionService;
    }
}
