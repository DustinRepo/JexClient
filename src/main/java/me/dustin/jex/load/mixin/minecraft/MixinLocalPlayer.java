package me.dustin.jex.load.mixin.minecraft;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.event.player.*;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    private EventPlayerPackets preEvent;

    @Shadow
    public Input input;

    @Shadow protected abstract void updateAutoJump(float f, float g);

    @Shadow public int sprintTime;

    @Shadow protected int sprintTriggerTime;

    @Shadow protected abstract void handleNetherPortalClient();

    @Shadow protected abstract boolean hasEnoughImpulseToStartSprinting();

    @Shadow protected abstract void moveTowardsClosestSpace(double x, double z);

    @Shadow private boolean crouching;

    @Shadow public abstract boolean isMovingSlowly();

    @Shadow @Final protected Minecraft minecraft;

    @Shadow private int autoJumpTime;

    @Shadow @Final public ClientPacketListener connection;

    @Shadow private boolean wasFallFlying;

    @Shadow private int waterVisionTime;

    @Shadow protected abstract boolean isControlledCamera();

    @Shadow public abstract boolean isRidingJumpable();

    @Shadow private int jumpRidingTicks;

    @Shadow private float jumpRidingScale;

    @Shadow public abstract float getJumpRidingScale();

    @Shadow protected abstract void sendRidingJump();

    public MixinLocalPlayer(ClientLevel world, GameProfile profile, ProfilePublicKey playerPublicKey) {
        super(world, profile, playerPublicKey);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initPlayer(Minecraft client, ClientLevel world, ClientPacketListener networkHandler, StatsCounter stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting, CallbackInfo ci) {
        CommandManagerJex.INSTANCE.registerCommands(networkHandler);
    }

    @Override
    public boolean isInWater() {
        EventIsPlayerTouchingWater eventIsPlayerTouchingWater = new EventIsPlayerTouchingWater(super.isInWater()).run();
        if (eventIsPlayerTouchingWater.isCancelled())
            return eventIsPlayerTouchingWater.isTouchingWater();
        return super.isInWater();
    }

    @Override
    public Pose getPose() {
        EventGetPose eventGetPose = new EventGetPose(super.getPose()).run();
        if (eventGetPose.isCancelled())
            return eventGetPose.getPose();
        return super.getPose();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/player/LocalPlayer.isPassenger()Z"))
    public void tick(CallbackInfo ci) {
        LocalPlayer me = (LocalPlayer) (Object) this;
        preEvent = new EventPlayerPackets(me.getViewYRot(1), me.getViewXRot(1), me.isOnGround()).run();
        if (preEvent.isCancelled())
            ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    public void tickEnd(CallbackInfo ci) {
        new EventPlayerPackets().run();
    }

    @Inject(method = "chat(Ljava/lang/String;Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String string, Component component, CallbackInfo ci) {
        EventSendMessage eventSendMessage = new EventSendMessage(string, component != null).run();
        if (eventSendMessage.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MoverType type, Vec3 movement, CallbackInfo ci) {
        if (type == MoverType.SELF) {
            EventMove eventMove = new EventMove(movement.x, movement.y, movement.z).run();
            movement = new Vec3(eventMove.getX(), eventMove.getY(), eventMove.getZ());
            double d = this.getX();
            double e = this.getZ();
            super.move(type, movement);
            this.updateAutoJump((float) (this.getX() - d), (float) (this.getZ() - e));
            ci.cancel();
        }
    }

    @Inject(method = "moveTowardsClosestSpace", at = @At("INVOKE"), cancellable = true)
    public void pushOut(double x, double z, CallbackInfo ci) {
        EventPushOutOfBlocks eventPushOutOfBlocks = new EventPushOutOfBlocks().run();
        if (eventPushOutOfBlocks.isCancelled())
            ci.cancel();
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    public void setSprinting(boolean sprinting, CallbackInfo ci) {
        EventSetSprint eventSetSprint = new EventSetSprint(sprinting).run();
        if (eventSetSprint.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "handleNetherPortalClient", at = @At(value = "INVOKE", target = "net/minecraft/client/player/LocalPlayer.closeContainer()V"))
    public void closeContainerOverride(LocalPlayer me) {
        EventPortalCloseGUI eventPortalCloseGUI = new EventPortalCloseGUI().run();
        if (!eventPortalCloseGUI.isCancelled())
            me.closeContainer();
    }

    @Redirect(method = "handleNetherPortalClient", at = @At(value = "INVOKE", target = "net/minecraft/client/Minecraft.setScreen (Lnet/minecraft/client/gui/screens/Screen;)V"))
    public void nullScreenOverride(Minecraft minecraftClient, Screen screen) {
        EventPortalCloseGUI eventPortalCloseGUI = new EventPortalCloseGUI().run();
        if (!eventPortalCloseGUI.isCancelled())
            minecraftClient.setScreen(screen);
    }

    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    public void tickMovePre(CallbackInfo ci) {
        new EventPlayerUpdates(EventPlayerUpdates.Mode.PRE).run();
        ++this.sprintTime;
        if (this.sprintTriggerTime > 0) {
            --this.sprintTriggerTime;
        }

        this.handleNetherPortalClient();
        boolean bl = this.input.jumping;
        boolean bl2 = this.input.shiftKeyDown;
        boolean bl3 = this.hasEnoughImpulseToStartSprinting();
        this.crouching = !this.getAbilities().flying && !this.isSwimming() && this.canEnterPose(Pose.CROUCHING) && (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
        float f = Mth.clamp(0.3F + EnchantmentHelper.getSneakingSpeedBonus(this), 0.0F, 1.0F);
        this.input.tick(this.isMovingSlowly(), f);
        this.minecraft.getTutorial().onInput(this.input);
        if ((this.isUsingItem() || AutoEat.isEating) && !this.isPassenger()) {
            EventSlowdown eventSlowdown = new EventSlowdown(EventSlowdown.State.USE_ITEM).run();
            if (!eventSlowdown.isCancelled()) {
                Input var10000 = this.input;
                var10000.leftImpulse *= 0.2F;
                var10000 = this.input;
                var10000.forwardImpulse *= 0.2F;
                this.sprintTriggerTime = 0;
            }
        }

        boolean bl4 = false;
        if (this.autoJumpTime > 0) {
            --this.autoJumpTime;
            bl4 = true;
            this.input.jumping = true;
        }

        if (!this.noPhysics) {
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35D, this.getZ() + (double)this.getBbWidth() * 0.35D);
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35D, this.getZ() - (double)this.getBbWidth() * 0.35D);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35D, this.getZ() - (double)this.getBbWidth() * 0.35D);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35D, this.getZ() + (double)this.getBbWidth() * 0.35D);
        }

        if (bl2) {
            this.sprintTriggerTime = 0;
        }

        boolean bl5 = (float) this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly;
        if ((this.onGround || this.isUnderWater()) && !bl2 && !bl3 && this.hasEnoughImpulseToStartSprinting() && !this.isSprinting() && bl5 && !this.isUsingItem() && !this.hasEffect(MobEffects.BLINDNESS)) {
            if (this.sprintTriggerTime <= 0 && !this.minecraft.options.keySprint.isDown()) {
                this.sprintTriggerTime = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && (!this.isInWater() || this.isUnderWater()) && this.hasEnoughImpulseToStartSprinting() && bl5 && !this.isUsingItem() && !this.hasEffect(MobEffects.BLINDNESS) && this.minecraft.options.keySprint.isDown()) {
            this.setSprinting(true);
        }

        boolean bl8;
        if (this.isSprinting()) {
            bl8 = !this.input.hasForwardImpulse() || !bl5;
            boolean bl7 = bl8 || this.horizontalCollision || this.isInWater() && !this.isUnderWater();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.shiftKeyDown && bl8 || !this.isInWater()) {
                    this.setSprinting(false);
                }
            } else if (bl7) {
                this.setSprinting(false);
            }
        }

        bl8 = false;
        if (this.getAbilities().mayfly) {
            if (this.minecraft.gameMode.isAlwaysFlying()) {
                if (!this.getAbilities().flying) {
                    this.getAbilities().flying = true;
                    bl8 = true;
                    this.onUpdateAbilities();
                }
            } else if (!bl && this.input.jumping && !bl4) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                } else if (!this.isSwimming()) {
                    this.getAbilities().flying = !this.getAbilities().flying;
                    bl8 = true;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }

        if (this.input.jumping && !bl8 && !bl && !this.getAbilities().flying && !this.isPassenger() && !this.onClimbable()) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.CHEST);
            if (itemStack.getItem() == Items.ELYTRA && ElytraItem.isFlyEnabled(itemStack) && this.tryToStartFallFlying()) {
                this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        }

        this.wasFallFlying = this.isFallFlying();
        if (this.isInWater() && this.input.shiftKeyDown &&  this.isAffectedByFluids()) {
            this.goDownInWater();
        }

        int i;
        if (this.isEyeInFluid(FluidTags.WATER)) {
            i = this.isSpectator() ? 10 : 1;
            this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
        } else if (this.waterVisionTime > 0) {
            this.isEyeInFluid(FluidTags.WATER);
            this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
        }

        if (this.getAbilities().flying && this.isControlledCamera()) {
            i = 0;
            if (this.input.shiftKeyDown) {
                --i;
            }

            if (this.input.jumping) {
                ++i;
            }

            if (i != 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)((float)i * this.getAbilities().getFlyingSpeed() * 3.0F), 0.0D));
            }
        }

        if (this.isRidingJumpable()) {
            PlayerRideableJumping playerRideableJumping = (PlayerRideableJumping)this.getVehicle();
            if (this.jumpRidingTicks < 0) {
                ++this.jumpRidingTicks;
                if (this.jumpRidingTicks == 0) {
                    this.jumpRidingScale = 0.0F;
                }
            }

            if (bl && !this.input.jumping) {
                this.jumpRidingTicks = -10;
                playerRideableJumping.onPlayerJump(Mth.floor(this.getJumpRidingScale() * 100.0F));
                this.sendRidingJump();
            } else if (!bl && this.input.jumping) {
                this.jumpRidingTicks = 0;
                this.jumpRidingScale = 0.0F;
            } else if (bl) {
                ++this.jumpRidingTicks;
                if (this.jumpRidingTicks < 10) {
                    this.jumpRidingScale = (float)this.jumpRidingTicks * 0.1F;
                } else {
                    this.jumpRidingScale = 0.8F + 2.0F / (float)(this.jumpRidingTicks - 9) * 0.1F;
                }
            }
        } else {
            this.jumpRidingScale = 0.0F;
        }

        super.aiStep();
        if (this.onGround && this.getAbilities().flying && !this.minecraft.gameMode.isAlwaysFlying()) {
            this.getAbilities().flying = false;
            this.onUpdateAbilities();
        }
        new EventPlayerUpdates(EventPlayerUpdates.Mode.POST).run();
        ci.cancel();
    }


    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/player/LocalPlayer.getYRot()F"))
    public float ridingYaw(LocalPlayer me) {
        return preEvent.getYaw();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/player/LocalPlayer.getXRot()F"))
    public float ridingPitch(LocalPlayer me) {
        return preEvent.getPitch();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "net/minecraft/client/player/LocalPlayer.getYRot()F"))
    public float redirYaw(LocalPlayer me) {
        return preEvent.getYaw();
    }

    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "net/minecraft/client/player/LocalPlayer.getXRot()F"))
    public float redirPitch(LocalPlayer me) {
        return preEvent.getPitch();
    }

    @Redirect(method = "sendPosition", at = @At(value = "FIELD", target = "net/minecraft/client/player/LocalPlayer.onGround:Z"))
    public boolean redirOG(LocalPlayer me) {
        return preEvent.isOnGround();
    }
}
