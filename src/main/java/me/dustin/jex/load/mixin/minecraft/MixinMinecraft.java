package me.dustin.jex.load.mixin.minecraft;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import me.dustin.jex.event.misc.*;
import me.dustin.jex.load.impl.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.client.world.ClientWorld;
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

    @Mutable
    @Shadow @Final private ProfileKeys profileKeys;

    @Shadow @Final private UserApiService userApiService;

    @Shadow @Final private YggdrasilAuthenticationService authenticationService;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void setRightClickDelayTimer(float timer) {
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

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    public void scheduleStop1(CallbackInfo ci) {
        new EventStop().run();
    }

    @Override
    public void setSessionService(MinecraftSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void setProfileKeys(ProfileKeys profileKeys) {
        this.profileKeys = profileKeys;
    }

    @Override
    public UserApiService getUserApiService() {
        return this.userApiService;
    }

    @Override
    public YggdrasilAuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }
}
