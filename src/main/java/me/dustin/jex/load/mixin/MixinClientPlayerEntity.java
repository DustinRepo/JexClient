package me.dustin.jex.load.mixin;

import com.mojang.authlib.GameProfile;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.event.player.*;
import me.dustin.jex.module.impl.player.AutoEat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {


    @Shadow
    public Input input;
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;
    @Shadow
    public int ticksSinceSprintingChanged;
    @Shadow
    @Final
    protected MinecraftClient client;
    @Shadow
    protected int ticksLeftToDoubleTapSprint;
    EventPlayerPackets preEvent;
    @Shadow
    private boolean usingItem;
    @Shadow
    private float field_3922;
    @Shadow
    private int field_3938;
    @Shadow
    private int underwaterVisibilityTicks;
    @Shadow
    private boolean field_3939;
    @Shadow
    private int ticksToNextAutojump;
    @Shadow
    private boolean inSneakingPose;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    protected abstract void autoJump(float dx, float dz);

    @Shadow
    protected abstract void startRidingJump();

    @Shadow
    public abstract float method_3151();

    @Shadow
    public abstract boolean hasJumpingMount();

    @Shadow
    protected abstract boolean isCamera();

    @Shadow
    protected abstract boolean isWalking();

    @Shadow
    protected abstract void pushOutOfBlocks(double x, double y);

    @Shadow
    public abstract boolean shouldSlowDown();

    @Shadow
    protected abstract void updateNausea();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.hasVehicle()Z"))
    public void tick(CallbackInfo ci) {
        ClientPlayerEntity me = (ClientPlayerEntity) (Object) this;
        preEvent = new EventPlayerPackets(me.yaw, me.pitch, me.isOnGround()).run();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    public void tickEnd(CallbackInfo ci) {
        new EventPlayerPackets().run();
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String string, CallbackInfo ci) {
        EventSendMessage eventSendMessage = new EventSendMessage(string).run();
        if (eventSendMessage.isCancelled()) {
            ci.cancel();
            return;
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (type == MovementType.SELF) {
            EventMove eventMove = new EventMove(movement.x, movement.y, movement.z).run();
            movement = new Vec3d(eventMove.getX(), eventMove.getY(), eventMove.getZ());
            double d = this.getX();
            double e = this.getZ();
            super.move(type, movement);
            this.autoJump((float) (this.getX() - d), (float) (this.getZ() - e));
            ci.cancel();
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("INVOKE"), cancellable = true)
    public void pushOut(double x, double y, CallbackInfo ci) {
        EventPushOutOfBlocks eventPushOutOfBlocks = new EventPushOutOfBlocks().run();
        if (eventPushOutOfBlocks.isCancelled())
            ci.cancel();
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    public void setSprinting(boolean sprinting, CallbackInfo ci) {
        EventSetSprint eventSetSprint = new EventSetSprint(sprinting).run();
        if (eventSetSprint.isCancelled()) {
            ci.cancel();
            return;
        }
    }

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.closeHandledScreen()V"))
    public void closeContainerOverride(ClientPlayerEntity me) {
        EventPortalCloseGUI eventPortalCloseGUI = new EventPortalCloseGUI().run();
        if (!eventPortalCloseGUI.isCancelled())
            me.closeHandledScreen();
    }

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "net/minecraft/client/MinecraftClient.openScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    public void nullScreenOverride(MinecraftClient minecraftClient, Screen screen) {
        EventPortalCloseGUI eventPortalCloseGUI = new EventPortalCloseGUI().run();
        if (!eventPortalCloseGUI.isCancelled())
            minecraftClient.openScreen(screen);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"), cancellable = true)
    public void tickMovePre(CallbackInfo ci) {
        new EventPlayerUpdates(EventPlayerUpdates.Mode.PRE).run();
        ++this.ticksSinceSprintingChanged;
        if (this.ticksLeftToDoubleTapSprint > 0) {
            --this.ticksLeftToDoubleTapSprint;
        }

        this.updateNausea();
        boolean bl = this.input.jumping;
        boolean bl2 = this.input.sneaking;
        boolean bl3 = this.isWalking();
        this.inSneakingPose = !this.abilities.flying && !this.isSwimming() && this.wouldPoseNotCollide(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.wouldPoseNotCollide(EntityPose.STANDING));
        this.input.tick(this.shouldSlowDown());
        this.client.getTutorialManager().onMovement(this.input);
        if ((this.isUsingItem() || AutoEat.isEating) && !this.hasVehicle()) {
            EventSlowdown eventSlowdown = new EventSlowdown(EventSlowdown.State.USE_ITEM).run();
            if (!eventSlowdown.isCancelled()) {
                Input var10000 = this.input;
                var10000.movementSideways *= 0.2F;
                var10000 = this.input;
                var10000.movementForward *= 0.2F;
                this.ticksLeftToDoubleTapSprint = 0;
            }
        }

        boolean bl4 = false;
        if (this.ticksToNextAutojump > 0) {
            --this.ticksToNextAutojump;
            bl4 = true;
            this.input.jumping = true;
        }

        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double) this.getWidth() * 0.35D, this.getZ() + (double) this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() - (double) this.getWidth() * 0.35D, this.getZ() - (double) this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() + (double) this.getWidth() * 0.35D, this.getZ() - (double) this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() + (double) this.getWidth() * 0.35D, this.getZ() + (double) this.getWidth() * 0.35D);
        }

        if (bl2) {
            this.ticksLeftToDoubleTapSprint = 0;
        }

        boolean bl5 = (float) this.getHungerManager().getFoodLevel() > 6.0F || this.abilities.allowFlying;
        if ((this.onGround || this.isSubmergedInWater()) && !bl2 && !bl3 && this.isWalking() && !this.isSprinting() && bl5 && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS)) {
            if (this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.keySprint.isPressed()) {
                this.ticksLeftToDoubleTapSprint = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && (!this.isTouchingWater() || this.isSubmergedInWater()) && this.isWalking() && bl5 && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && this.client.options.keySprint.isPressed()) {
            this.setSprinting(true);
        }

        boolean bl8;
        if (this.isSprinting()) {
            bl8 = !this.input.hasForwardMovement() || !bl5;
            boolean bl7 = bl8 || this.horizontalCollision || this.isTouchingWater() && !this.isSubmergedInWater();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.sneaking && bl8 || !this.isTouchingWater()) {
                    this.setSprinting(false);
                }
            } else if (bl7) {
                this.setSprinting(false);
            }
        }

        bl8 = false;
        if (this.abilities.allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!this.abilities.flying) {
                    this.abilities.flying = true;
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!bl && this.input.jumping && !bl4) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    this.abilities.flying = !this.abilities.flying;
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }

        if (this.input.jumping && !bl8 && !bl && !this.abilities.flying && !this.hasVehicle() && !this.isClimbing()) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.getItem() == Items.ELYTRA && ElytraItem.isUsable(itemStack) && this.checkFallFlying()) {
                this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        this.field_3939 = this.isFallFlying();
        if (this.isTouchingWater() && this.input.sneaking && this.method_29920()) {
            this.knockDownwards();
        }

        int j;
        if (this.isSubmergedIn(FluidTags.WATER)) {
            j = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + j, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }

        if (this.abilities.flying && this.isCamera()) {
            j = 0;
            if (this.input.sneaking) {
                --j;
            }

            if (this.input.jumping) {
                ++j;
            }

            if (j != 0) {
                this.setVelocity(this.getVelocity().add(0.0D, (double) ((float) j * this.abilities.getFlySpeed() * 3.0F), 0.0D));
            }
        }

        if (this.hasJumpingMount()) {
            JumpingMount jumpingMount = (JumpingMount) this.getVehicle();
            if (this.field_3938 < 0) {
                ++this.field_3938;
                if (this.field_3938 == 0) {
                    this.field_3922 = 0.0F;
                }
            }

            if (bl && !this.input.jumping) {
                this.field_3938 = -10;
                jumpingMount.setJumpStrength(MathHelper.floor(this.method_3151() * 100.0F));
                this.startRidingJump();
            } else if (!bl && this.input.jumping) {
                this.field_3938 = 0;
                this.field_3922 = 0.0F;
            } else if (bl) {
                ++this.field_3938;
                if (this.field_3938 < 10) {
                    this.field_3922 = (float) this.field_3938 * 0.1F;
                } else {
                    this.field_3922 = 0.8F + 2.0F / (float) (this.field_3938 - 9) * 0.1F;
                }
            }
        } else {
            this.field_3922 = 0.0F;
        }

        super.tickMovement();
        if (this.onGround && this.abilities.flying && !this.client.interactionManager.isFlyingLocked()) {
            this.abilities.flying = false;
            this.sendAbilitiesUpdate();
        }
        new EventPlayerUpdates(EventPlayerUpdates.Mode.POST).run();
        ci.cancel();
    }


    @Redirect(method = "tick", at = @At(value = "FIELD", target = "net/minecraft/client/network/ClientPlayerEntity.yaw:F"))
    public float ridingYaw(ClientPlayerEntity me) {
        return preEvent.getYaw();
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "net/minecraft/client/network/ClientPlayerEntity.pitch:F"))
    public float ridingPitch(ClientPlayerEntity me) {
        return preEvent.getPitch();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "net/minecraft/client/network/ClientPlayerEntity.yaw:F"))
    public float redirYaw(ClientPlayerEntity me) {
        return preEvent.getYaw();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "net/minecraft/client/network/ClientPlayerEntity.pitch:F"))
    public float redirPitch(ClientPlayerEntity me) {
        return preEvent.getPitch();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "net/minecraft/client/network/ClientPlayerEntity.onGround:Z"))
    public boolean redirOG(ClientPlayerEntity me) {
        return preEvent.isOnGround();
    }
}
