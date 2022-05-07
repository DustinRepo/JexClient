package me.dustin.jex.helper.math.vector;

import net.minecraft.core.Vec3i;

public class Vector3I {
    
    public int x,y,z;

    public Vector3I() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3I(Vec3i vec3i) {
        this.x = vec3i.getX();
        this.y = vec3i.getY();
        this.z = vec3i.getZ();
    }

    public Vector3I(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public Vector3I multiply(int mulX, int mulY, int mulZ) {
        this.x *= mulX;
        this.y *= mulY;
        this.z *= mulZ;
        return this;
    }

    public Vector3I divide(int divX, int divY, int divZ) {
        this.x /= divX;
        this.y /= divY;
        this.z /= divZ;
        return this;
    }

    public Vector3I add(int addX, int addY, int addZ) {
        this.x += addX;
        this.y += addY;
        this.z += addZ;
        return this;
    }

    public Vector3I subtract(int subX, int subY, int subZ) {
        this.x -= subX;
        this.y -= subY;
        this.z -= subZ;
        return this;
    }

    public Vector3I multiply(Vector3I vector3I) {
        return multiply(vector3I.getX(), vector3I.getY(), vector3I.getZ());
    }

    public Vector3I divide(Vector3I vector3I) {
        return divide(vector3I.getX(), vector3I.getY(), vector3I.getZ());
    }

    public Vector3I add(Vector3I vector3I) {
        return add(vector3I.getX(), vector3I.getY(), vector3I.getZ());
    }

    public Vector3I subtract(Vector3I vector3I) {
        return subtract(vector3I.getX(), vector3I.getY(), vector3I.getZ());
    }

    public Vector3I multiply(int mul) {
        this.x *= mul;
        this.y *= mul;
        this.z *= mul;
        return this;
    }

    public Vector3I divide(int div) {
        this.x /= div;
        this.y /= div;
        this.z /= div;
        return this;
    }

    public Vector3I add(int add) {
        this.x += add;
        this.y += add;
        this.z += add;
        return this;
    }

    public Vector3I subtract(int sub) {
        this.x -= sub;
        this.y -= sub;
        this.z -= sub;
        return this;
    }

    public Vec3i toMinecraft() {
        return new Vec3i(x, y, z);
    }
}
