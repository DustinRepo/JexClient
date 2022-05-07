package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.option.annotate.Op;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Random;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Add comic \"Pow!\" and others to the game")
public class SuperheroFX extends Feature{

    @Op(name = "Visible Only")
    public boolean visibleOnly = true;
    @Op(name = "Max Age (MS)", min = 250, max = 2000)
    public int maxAge = 500;
    @Op(name = "Size", min = 8, max = 64, inc = 4)
    public int size = 32;
    @Op(name = "Particle Count", min = 1, max = 20)
    public int particleCount = 5;

    private final ArrayList<LivingEntity> attacked = new ArrayList<>();
    private final ArrayList<KapowParticle> particles = new ArrayList<>();

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
            attacked.add(livingEntity);
        }
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        for (int i = 0; i < attacked.size(); i++) {
            LivingEntity livingEntity = attacked.get(i);
            Random random = new Random();
            for (int j = 0; j < (1 +random.nextInt(particleCount)); j++) {
                FXType type = FXType.values()[random.nextInt(FXType.values().length)];
                float sideOffset = livingEntity.getBbWidth() / 1.5f;
                float heightOffset = livingEntity.getBbHeight() / 2;
                double x = livingEntity.getX() - sideOffset + (random.nextDouble() * (sideOffset * 2));
                double y = livingEntity.getY() + (double) (livingEntity.getBbHeight() / 2) - heightOffset + (random.nextFloat() * (heightOffset * 2));
                double z = livingEntity.getZ() - sideOffset + (random.nextDouble() * (sideOffset * 2));
                Vec3 vec3d = new Vec3(x, y, z);
                KapowParticle kapowParticle = new KapowParticle(vec3d, type);
                kapowParticle.setTwoDPosition(Render2DHelper.INSTANCE.to2D(kapowParticle.getPosition(), event.getPoseStack()));
                particles.add(kapowParticle);
            }
            attacked.remove(i);
        }
        for (int i = 0; i < particles.size(); i++) {
            KapowParticle kapowParticle = particles.get(i);
            if (kapowParticle.getAge() <= 0) {
                particles.remove(i);
                continue;
            }
            kapowParticle.setTwoDPosition(Render2DHelper.INSTANCE.to2D(kapowParticle.getPosition(), event.getPoseStack()));
        }
    });

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        particles.forEach(particle -> particle.render(event.getPoseStack()));
    });

    public class KapowParticle {
        private final ResourceLocation identifier;
        private Vec3 position;
        private Vec3 twoDPosition;
        private FXType fxType;
        private int age = 200;
        private final StopWatch stopWatch;

        public KapowParticle(Vec3 position, FXType fxType) {
            this.position = position;
            this.fxType = fxType;
            this.identifier = new ResourceLocation("jex", "comic/" + fxType.name().toLowerCase() + ".png");
            stopWatch = new StopWatch();
            stopWatch.reset();
        }

        public void render(PoseStack matrixStack) {
            if (stopWatch.hasPassed(maxAge))
                this.age = 0;
            if (visibleOnly) {
                Vec3 vec3d = new Vec3(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getEyeY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
                Vec3 vec3d2 = new Vec3(position.x(), position.y(), position.z());
                if (vec3d2.distanceTo(vec3d) > 128.0D || Wrapper.INSTANCE.getWorld().clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, Wrapper.INSTANCE.getLocalPlayer())).getType() != HitResult.Type.MISS)
                    return;
            }
            if (Render2DHelper.INSTANCE.isOnScreen(twoDPosition)) {
                Render2DHelper.INSTANCE.bindTexture(identifier);
                GuiComponent.blit(matrixStack, (int)twoDPosition.x - (size / 2), (int)twoDPosition.y - (size / 2), 0, 0, size, size, size, size);
            }
        }

        public Vec3 getPosition() {
            return position;
        }

        public void setPosition(Vec3 position) {
            this.position = position;
        }

        public ResourceLocation getIdentifier() {
            return identifier;
        }

        public FXType getFxType() {
            return fxType;
        }

        public void setFxType(FXType fxType) {
            this.fxType = fxType;
        }

        public int getAge() {
            return age;
        }

        public Vec3 getTwoDPosition() {
            return twoDPosition;
        }

        public void setTwoDPosition(Vec3 twoDPosition) {
            this.twoDPosition = twoDPosition;
        }
    }

    public enum FXType {
        KAPOW, WHAM, BOOM, POW
    }
}
