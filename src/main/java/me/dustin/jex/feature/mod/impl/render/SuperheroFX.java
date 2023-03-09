package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import java.util.ArrayList;
import java.util.Random;

public class SuperheroFX extends Feature{

    public final Property<Boolean> visibleOnlyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Visible Only")
            .value(true)
            .build();
    public final Property<Long> maxAgeProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Max Age (MS)")
            .value(500L)
            .min(250)
            .max(2000)
            .build();
    public final Property<Integer> sizeProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Size")
            .value(32)
            .min(8)
            .max(64)
            .inc(4)
            .build();
    public final Property<Integer> particleCountProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Particle Count")
            .value(5)
            .min(1)
            .max(20)
            .build();

    private final ArrayList<LivingEntity> attacked = new ArrayList<>();
    private final ArrayList<KapowParticle> particles = new ArrayList<>();

    public SuperheroFX() {
        super(Category.VISUAL);
    }

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
            for (int j = 0; j < (1 +random.nextInt(particleCountProperty.value())); j++) {
                FXType type = FXType.values()[random.nextInt(FXType.values().length)];
                float sideOffset = livingEntity.getWidth() / 1.5f;
                float heightOffset = livingEntity.getHeight() / 2;
                double x = livingEntity.getX() - sideOffset + (random.nextDouble() * (sideOffset * 2));
                double y = livingEntity.getY() + (double) (livingEntity.getHeight() / 2) - heightOffset + (random.nextFloat() * (heightOffset * 2));
                double z = livingEntity.getZ() - sideOffset + (random.nextDouble() * (sideOffset * 2));
                Vec3d vec3d = new Vec3d(x, y, z);
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
        private final Identifier identifier;
        private Vec3d position;
        private Vec3d twoDPosition;
        private FXType fxType;
        private int age = 200;
        private final StopWatch stopWatch;

        public KapowParticle(Vec3d position, FXType fxType) {
            this.position = position;
            this.fxType = fxType;
            this.identifier = new Identifier("jex", "comic/" + fxType.name().toLowerCase() + ".png");
            stopWatch = new StopWatch();
            stopWatch.reset();
        }

        public void render(MatrixStack matrixStack) {
            if (stopWatch.hasPassed(maxAgeProperty.value()))
                this.age = 0;
            if (visibleOnlyProperty.value()) {
                Vec3d vec3d = new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getEyeY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
                Vec3d vec3d2 = new Vec3d(position.getX(), position.getY(), position.getZ());
                if (vec3d2.distanceTo(vec3d) > 128.0D || Wrapper.INSTANCE.getWorld().raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, Wrapper.INSTANCE.getLocalPlayer())).getType() != HitResult.Type.MISS)
                    return;
            }
            if (Render2DHelper.INSTANCE.isOnScreen(twoDPosition)) {
                Render2DHelper.INSTANCE.bindTexture(identifier);
                DrawableHelper.drawTexture(matrixStack, (int)twoDPosition.x - (sizeProperty.value() / 2), (int)twoDPosition.y - (sizeProperty.value() / 2), 0, 0, sizeProperty.value(), sizeProperty.value(), sizeProperty.value(), sizeProperty.value());
            }
        }

        public Vec3d getPosition() {
            return position;
        }

        public void setPosition(Vec3d position) {
            this.position = position;
        }

        public Identifier getIdentifier() {
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

        public Vec3d getTwoDPosition() {
            return twoDPosition;
        }

        public void setTwoDPosition(Vec3d twoDPosition) {
            this.twoDPosition = twoDPosition;
        }
    }

    public enum FXType {
        KAPOW, WHAM, BOOM, POW
    }
}
