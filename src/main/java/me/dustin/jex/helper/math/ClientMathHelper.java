package me.dustin.jex.helper.math;

import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public enum ClientMathHelper {
    INSTANCE;

    public final float PI = 3.14159f;

    private final Random rng = new Random();

    public float getAngleDifference(float alpha, float beta) {
        float phi = Math.abs(beta - alpha) % 360;       // This is either the DistanceCheck or 360 - DistanceCheck
        float distance = phi > 180 ? 360 - phi : phi;
        return distance;
    }

    public float cap(float i, float j, float k) {
        if (i > j) {
            i = j;
        }
        if (i < k) {
            i = k;
        }
        return i;
    }

    public <T extends Comparable<T>> T clamp(T val, T min, T max) {
        return val.compareTo(min) < 0 ? min : val.compareTo(max) > 0 ? max : val;
    }

    private double lastMovementFovMultiplier, movementFovMultiplier;

    public double getFOV(Camera camera, float tickDelta, boolean changingFov) {
        double d = 70.0D;
        if (changingFov) {
            d = Wrapper.INSTANCE.getOptions().fov;
            d *= (double)MathHelper.lerp(tickDelta, this.lastMovementFovMultiplier, this.movementFovMultiplier);
        }

        if (camera.getFocusedEntity() instanceof LivingEntity && ((LivingEntity)camera.getFocusedEntity()).isDead()) {
            float f = Math.min((float)((LivingEntity)camera.getFocusedEntity()).deathTime + tickDelta, 20.0F);
            d /= (double)((1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F);
        }

        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        if (cameraSubmersionType == CameraSubmersionType.LAVA || cameraSubmersionType == CameraSubmersionType.WATER) {
            d *= (double)MathHelper.lerp(Wrapper.INSTANCE.getOptions().fovEffectScale, 1.0F, 0.85714287F);
        }

        updateMovementFovMultiplier();
        return d;
    }

    private void updateMovementFovMultiplier() {
        float f = 1.0F;
        if (Wrapper.INSTANCE.getMinecraft().getCameraEntity() instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity abstractClientPlayerEntity = (AbstractClientPlayerEntity)Wrapper.INSTANCE.getMinecraft().getCameraEntity();
            f = abstractClientPlayerEntity.getSpeed();
        }

        this.lastMovementFovMultiplier = this.movementFovMultiplier;
        this.movementFovMultiplier += (f - this.movementFovMultiplier) * 0.5F;
        if (this.movementFovMultiplier > 1.5F) {
            this.movementFovMultiplier = 1.5F;
        }

        if (this.movementFovMultiplier < 0.1F) {
            this.movementFovMultiplier = 0.1F;
        }

    }

    public float getRandom() {
        return rng.nextFloat();
    }

    public int getRandom(int cap) {
        return rng.nextInt(cap);
    }

    public int getRandom(int floor, int cap) {
        return floor + rng.nextInt(cap - floor + 1);
    }

    public int randInt(int min, int max) {
        return rng.nextInt(max - min + 1) + min;
    }

    public float randFloat(float min, float max) {
        return min + rng.nextFloat() * (max - min);
    }

    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public boolean isSame(BlockPos blockPos, BlockPos blockPos1) {
        if (blockPos == null || blockPos1 == null)
            return false;
        return blockPos.getX() == blockPos1.getX() && blockPos.getY() == blockPos1.getY() && blockPos.getZ() == blockPos1.getZ();
    }

    public float getSimilarity(String string1, String string2) {
        int halflen = Math.min(string1.length(), string2.length()) / 2 + Math.min(string1.length(), string2.length()) % 2;

        StringBuffer common1 = getCommonCharacters(string1, string2, halflen);
        StringBuffer common2 = getCommonCharacters(string2, string1, halflen);
        if ((common1.length() == 0) || (common2.length() == 0)) {
            return 0.0F;
        }
        if (common1.length() != common2.length()) {
            return 0.0F;
        }
        int transpositions = 0;
        int n = common1.length();
        for (int i = 0; i < n; i++) {
            if (common1.charAt(i) != common2.charAt(i)) {
                transpositions++;
            }
        }
        transpositions = (int) (transpositions / 2.0F);

        return (common1.length() / string1.length() + common2.length() / string2.length() + (common1.length() - transpositions) / common1.length()) / 3.0F;
    }

    private StringBuffer getCommonCharacters(String string1, String string2, int distanceSep) {
        StringBuffer returnCommons = new StringBuffer();

        StringBuffer copy = new StringBuffer(string2);

        int n = string1.length();
        int m = string2.length();
        for (int i = 0; i < n; i++) {
            char ch = string1.charAt(i);

            boolean foundIt = false;
            for (int j = Math.max(0, i - distanceSep); (!foundIt) && (j < Math.min(i + distanceSep, m - 1)); j++) {
                if (copy.charAt(j) == ch) {
                    foundIt = true;

                    returnCommons.append(ch);

                    copy.setCharAt(j, '\000');
                }
            }
        }
        return returnCommons;
    }

    public double roundToPlace(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public float getDistance(Vec3d vec, Vec3d vec1) {
        double d0 = vec.x - vec1.x;
        double d1 = vec.y - vec1.y;
        double d2 = vec.z - vec1.z;
        return MathHelper.sqrt((float)(d0 * d0 + d1 * d1 + d2 * d2));
    }

    public float getDistance2D(Vec2f vec, Vec2f vec1) {
        double d0 = vec.x - vec1.x;
        double d1 = vec.y - vec1.y;
        return MathHelper.sqrt((float)(d0 * d0 + d1 * d1));
    }

    public float getDistance2D(Vec3d vec, Vec3d vec1) {
        double d0 = vec.x - vec1.x;
        double d1 = vec.z - vec1.z;
        return MathHelper.sqrt((float)(d0 * d0 + d1 * d1));
    }

    public Vec3d getVec(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }

    public Vec3d getVec(BlockPos blockPos) {
        return new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}
