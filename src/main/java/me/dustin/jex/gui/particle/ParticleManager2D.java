package me.dustin.jex.gui.particle;

import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public enum ParticleManager2D {
    INSTANCE;
    private ArrayList<Particle> particles = new ArrayList<>();

    public void update() {
        for (Particle particle : particles) {
            particle.update();
        }
    }

    public void add(float x, float y) {
        particles.add(new Particle(x, y));
    }

    public void remove(Particle particle) {
        particles.remove(particle);
    }

    public ArrayList<Particle> getParticles() {
        return particles;
    }

    public class Particle {

        Vec3d position;
        Vec3d direction;
        float speed = 0.0f;
        float acceleration = 0.1f;
        Timer timer = new Timer();

        public Particle(float x, float y) {
            this.position = new Vec3d(x, y, 0);
            this.direction = new Vec3d(ClientMathHelper.INSTANCE.getRandom(-3, 3), ClientMathHelper.INSTANCE.getRandom(-3, 3), 0);
            while (direction.x == 0 || direction.y == 0) {
                this.direction = new Vec3d(ClientMathHelper.INSTANCE.getRandom(-4, 4), ClientMathHelper.INSTANCE.getRandom(-4, 4), 0);
            }
        }

        private void update() {
            acceleration = 0.0001f;
            position = position.add(direction.multiply(speed));
            speed += acceleration;
            if (position.y > Render2DHelper.INSTANCE.getScaledHeight()) {
                position = new Vec3d(position.x, Render2DHelper.INSTANCE.getScaledHeight(), 0);
                direction = new Vec3d(direction.x, -direction.y, 0);
                speed = 0.0f;
            } else if (position.y < 0) {
                position = new Vec3d(position.x, 0, 0);
                direction = new Vec3d(direction.x, -direction.y, 0);
                speed = 0.0f;
            }

            if (position.x > Render2DHelper.INSTANCE.getScaledWidth()) {
                position = new Vec3d(Render2DHelper.INSTANCE.getScaledWidth(), position.y, 0);
                direction = new Vec3d(-direction.x, direction.y, 0);
                speed = 0.0f;
            } else if (position.x < 0) {
                position = new Vec3d(0, position.y, 0);
                direction = new Vec3d(-direction.x, direction.y, 0);
                speed = 0.0f;
            }
        }

        public void draw(MatrixStack matrixStack) {
            if (timer.hasPassed(200)) {
                ParticleManager2D.INSTANCE.update();
            }
            Render2DHelper.INSTANCE.fill(matrixStack, (float) position.x, (float) position.y, (float) position.x + 1, (float) position.y + 1, ColorHelper.INSTANCE.getClientColor());
        }

        public Vec3d getPosition() {
            return position;
        }
    }
}
