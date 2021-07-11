package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Random;

@Feature.Manifest(name = "SuperheroFX", category = Feature.Category.VISUAL, description = "Add comic \"Pow!\" and others to the game")
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

    @EventListener(events = {EventAttackEntity.class, EventRender3D.class, EventRender2D.class})
    private void runMethod(Event event) {
        if (event instanceof EventAttackEntity eventAttackEntity) {
            if (eventAttackEntity.getEntity() instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                attacked.add(livingEntity);
            }
        } else if (event instanceof EventRender3D eventRender3D) {
            for (int i = 0; i < attacked.size(); i++) {
                LivingEntity livingEntity = attacked.get(i);
                Random random = new Random();
                for (int j = 0; j < (1 +random.nextInt(particleCount)); j++) {
                    FXType type = FXType.values()[random.nextInt(FXType.values().length)];
                    float sideOffset = livingEntity.getWidth() / 1.5f;
                    float heightOffset = livingEntity.getHeight() / 2;
                    double x = livingEntity.getX() - sideOffset + (random.nextDouble() * (sideOffset * 2));
                    double y = livingEntity.getY() + (double) (livingEntity.getHeight() / 2) - heightOffset + (random.nextFloat() * (heightOffset * 2));
                    double z = livingEntity.getZ() - sideOffset + (random.nextDouble() * (sideOffset * 2));
                    Vec3d vec3d = new Vec3d(x, y, z);
                    KapowParticle kapowParticle = new KapowParticle(vec3d, type);
                    kapowParticle.setTwoDPosition(Render2DHelper.INSTANCE.to2D(kapowParticle.getPosition(), eventRender3D.getMatrixStack()));
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
                kapowParticle.setTwoDPosition(Render2DHelper.INSTANCE.to2D(kapowParticle.getPosition(), eventRender3D.getMatrixStack()));
            }
        } else if (event instanceof EventRender2D eventRender2D) {
            particles.forEach(particle -> {
                particle.render(eventRender2D.getMatrixStack());
            });
        }
    }

    public class KapowParticle {
        private Vec3d position;
        private Vec3d twoDPosition;
        private Identifier identifier;
        private FXType fxType;
        private int age = 200;
        private Timer timer;

        public KapowParticle(Vec3d position, FXType fxType) {
            this.position = position;
            this.fxType = fxType;
            this.identifier = new Identifier("jex", "comic/" + fxType.name().toLowerCase() + ".png");
            timer = new Timer();
            timer.reset();
        }

        public void render(MatrixStack matrixStack) {
            if (timer.hasPassed(maxAge))
                this.age = 0;
            if (visibleOnly) {
                Vec3d vec3d = new Vec3d(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getEyeY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
                Vec3d vec3d2 = new Vec3d(position.getX(), position.getY(), position.getZ());
                if (vec3d2.distanceTo(vec3d) > 128.0D || Wrapper.INSTANCE.getWorld().raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, Wrapper.INSTANCE.getLocalPlayer())).getType() != HitResult.Type.MISS)
                    return;
            }
            if (Render2DHelper.INSTANCE.isOnScreen(twoDPosition)) {
                Render2DHelper.INSTANCE.bindTexture(identifier);
                DrawableHelper.drawTexture(matrixStack, (int)twoDPosition.x - (size / 2), (int)twoDPosition.y - (size / 2), 0, 0, size, size, size, size);
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
