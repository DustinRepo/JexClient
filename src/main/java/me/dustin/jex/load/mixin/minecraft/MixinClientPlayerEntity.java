package me.dustin.jex.load.mixin.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import me.dustin.jex.event.chat.EventSendMessage;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.event.player.*;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.load.impl.IClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageMetadata;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements IClientPlayerEntity {

    private EventPlayerPackets preEvent;

    @Shadow
    public Input input;

    @Shadow protected abstract void autoJump(float dx, float dz);

    @Shadow public int ticksSinceSprintingChanged;

    @Shadow protected int ticksLeftToDoubleTapSprint;

    @Shadow protected abstract void updateNausea();

    @Shadow protected abstract boolean isWalking();

    @Shadow private boolean inSneakingPose;

    @Shadow public abstract boolean shouldSlowDown();

    @Shadow @Final protected MinecraftClient client;

    @Shadow private int ticksToNextAutojump;

    @Shadow protected abstract void pushOutOfBlocks(double x, double z);

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    @Shadow private boolean falling;

    @Shadow private int underwaterVisibilityTicks;

    @Shadow protected abstract boolean isCamera();

    @Shadow public abstract boolean hasJumpingMount();

    @Shadow private float mountJumpStrength;

    @Shadow private int field_3938;

    @Shadow public abstract float getMountJumpStrength();

    @Shadow protected abstract void startRidingJump();


    @Shadow protected abstract ArgumentSignatureDataMap signArguments(MessageMetadata signer, ParseResults<CommandSource> parseResults, @Nullable Text preview, LastSeenMessageList lastSeenMessages);

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile, PlayerPublicKey playerPublicKey) {
        super(world, profile, playerPublicKey);
    }

    @Override
    public boolean isTouchingWater() {
        EventIsPlayerTouchingWater eventIsPlayerTouchingWater = new EventIsPlayerTouchingWater(super.isTouchingWater()).run();
        if (eventIsPlayerTouchingWater.isCancelled())
            return eventIsPlayerTouchingWater.isTouchingWater();
        return super.isTouchingWater();
    }

    @Override
    public EntityPose getPose() {
        EventGetPose eventGetPose = new EventGetPose(super.getPose()).run();
        if (eventGetPose.isCancelled())
            return eventGetPose.getPose();
        return super.getPose();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.hasVehicle()Z"), cancellable = true)
    public void tick(CallbackInfo ci) {
        ClientPlayerEntity me = (ClientPlayerEntity) (Object) this;
        preEvent = new EventPlayerPackets(me.getYaw(1), me.getPitch(1), me.isOnGround()).run();
        if (preEvent.isCancelled())
            ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    public void tickEnd(CallbackInfo ci) {
        new EventPlayerPackets().run();
    }

    @Inject(method = "sendChatMessagePacket", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String string, Text text, CallbackInfo ci) {
        EventSendMessage eventSendMessage = new EventSendMessage(string).run();
        if (eventSendMessage.isCancelled()) {
            ci.cancel();
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

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.closeHandledScreen()V"))
    public void closeContainerOverride(ClientPlayerEntity me) {
        EventPortalCloseGUI eventPortalCloseGUI = new EventPortalCloseGUI().run();
        if (!eventPortalCloseGUI.isCancelled())
            me.closeHandledScreen();
    }

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "net/minecraft/client/MinecraftClient.setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    public void nullScreenOverride(MinecraftClient minecraftClient, Screen screen) {
        EventPortalCloseGUI eventPortalCloseGUI = new EventPortalCloseGUI().run();
        if (!eventPortalCloseGUI.isCancelled())
            minecraftClient.setScreen(screen);
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
        this.inSneakingPose = !this.getAbilities().flying && !this.isSwimming() && this.wouldPoseNotCollide(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.wouldPoseNotCollide(EntityPose.STANDING));
        float f = MathHelper.clamp(0.3F + EnchantmentHelper.getSwiftSneakSpeedBoost(this), 0.0F, 1.0F);
        this.input.tick(this.shouldSlowDown(), f);
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
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35D, this.getZ() + (double)this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35D, this.getZ() - (double)this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35D, this.getZ() - (double)this.getWidth() * 0.35D);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35D, this.getZ() + (double)this.getWidth() * 0.35D);
        }

        if (bl2) {
            this.ticksLeftToDoubleTapSprint = 0;
        }

        boolean bl5 = (float) this.getHungerManager().getFoodLevel() > 6.0F || this.getAbilities().allowFlying;
        if ((this.onGround || this.isSubmergedInWater()) && !bl2 && !bl3 && this.isWalking() && !this.isSprinting() && bl5 && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS)) {
            if (this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.sprintKey.isPressed()) {
                this.ticksLeftToDoubleTapSprint = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && (!this.isTouchingWater() || this.isSubmergedInWater()) && this.isWalking() && bl5 && !this.isUsingItem() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && this.client.options.sprintKey.isPressed()) {
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
        if (this.getAbilities().allowFlying) {
            if (this.client.interactionManager.isFlyingLocked()) {
                if (!this.getAbilities().flying) {
                    this.getAbilities().flying = true;
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!bl && this.input.jumping && !bl4) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    this.getAbilities().flying = !this.getAbilities().flying;
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }

        if (this.input.jumping && !bl8 && !bl && !this.getAbilities().flying && !this.hasVehicle() && !this.isClimbing()) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack.getItem() == Items.ELYTRA && ElytraItem.isUsable(itemStack) && this.checkFallFlying()) {
                this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        this.falling = this.isFallFlying();
        if (this.isTouchingWater() && this.input.sneaking &&  this.shouldSwimInFluids()) {
            this.knockDownwards();
        }

        int i;
        if (this.isSubmergedIn(FluidTags.WATER)) {
            i = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + i, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }

        if (this.getAbilities().flying && this.isCamera()) {
            i = 0;
            if (this.input.sneaking) {
                --i;
            }

            if (this.input.jumping) {
                ++i;
            }

            if (i != 0) {
                this.setVelocity(this.getVelocity().add(0.0D, (double)((float)i * this.getAbilities().getFlySpeed() * 3.0F), 0.0D));
            }
        }

        if (this.hasJumpingMount()) {
            JumpingMount playerRideableJumping = (JumpingMount)this.getVehicle();
            if (this.field_3938 < 0) {
                ++this.field_3938;//jumpRidingTicks
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0F;
                }
            }

            if (bl && !this.input.jumping) {
                this.field_3938 = -10;
                playerRideableJumping.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0F));
                this.startRidingJump();
            } else if (!bl && this.input.jumping) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0F;
            } else if (bl) {
                ++this.field_3938;
                if (this.field_3938 < 10) {
                    this.mountJumpStrength = (float)this.field_3938 * 0.1F;
                } else {
                    this.mountJumpStrength = 0.8F + 2.0F / (float)(this.field_3938 - 9) * 0.1F;
                }
            }
        } else {
            this.mountJumpStrength = 0.0F;
        }

        super.tickMovement();
        if (this.onGround && this.getAbilities().flying && !this.client.interactionManager.isFlyingLocked()) {
            this.getAbilities().flying = false;
            this.sendAbilitiesUpdate();
        }
        new EventPlayerUpdates(EventPlayerUpdates.Mode.POST).run();
        ci.cancel();
    }


    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.getYaw()F"))
    public float ridingYaw(ClientPlayerEntity me) {
        return preEvent.getYaw();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.getPitch()F"))
    public float ridingPitch(ClientPlayerEntity me) {
        return preEvent.getPitch();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.getYaw()F"))
    public float redirYaw(ClientPlayerEntity me) {
        return preEvent.getYaw();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "net/minecraft/client/network/ClientPlayerEntity.getPitch()F"))
    public float redirPitch(ClientPlayerEntity me) {
        return preEvent.getPitch();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "net/minecraft/client/network/ClientPlayerEntity.onGround:Z"))
    public boolean redirOG(ClientPlayerEntity me) {
        return preEvent.isOnGround();
    }

    @Override
    public ArgumentSignatureDataMap callSignArguments(MessageMetadata signer, ParseResults<CommandSource> parseResults, @Nullable Text preview, LastSeenMessageList lastSeenMessages) {
        return this.signArguments(signer, parseResults, preview, lastSeenMessages);
    }
}
