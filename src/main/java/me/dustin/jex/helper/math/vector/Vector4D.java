package me.dustin.jex.helper.math.vector;

import me.dustin.jex.helper.math.Matrix4x4;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

public class Vector4D {

    public double x,y,z,w;

    public Vector4D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    public Vector4D(Vector4D vec3d) {
        this.x = vec3d.x;
        this.y = vec3d.y;
        this.z = vec3d.z;
        this.w = vec3d.w;
    }

    public Vector4D(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public Vector4D multiply(double mulX, double mulY, double mulZ, double mulW) {
        this.x *= mulX;
        this.y *= mulY;
        this.z *= mulZ;
        this.w *= mulW;
        return this;
    }

    public Vector4D divide(double divX, double divY, double divZ, double divW) {
        this.x /= divX;
        this.y /= divY;
        this.z /= divZ;
        this.w /= divW;
        return this;
    }

    public Vector4D add(double addX, double addY, double addZ, double addW) {
        this.x += addX;
        this.y += addY;
        this.z += addZ;
        this.w += addW;
        return this;
    }

    public Vector4D subtract(double subX, double subY, double subZ, double subW) {
        this.x -= subX;
        this.y -= subY;
        this.z -= subZ;
        this.w -= subW;
        return this;
    }

    public Vector4D transform(Matrix4f matrix4f) {
        return transform(Matrix4x4.copyFromRowMajor(matrix4f));
    }

    public Vector4D transform(Matrix4x4 matrix4x4) {
        double f = this.x;
        double g = this.y;
        double h = this.z;
        double i = this.w;
        this.x = matrix4x4.a00 * f + matrix4x4.a01 * g + matrix4x4.a02 * h + matrix4x4.a03 * i;
        this.y = matrix4x4.a10 * f + matrix4x4.a11 * g + matrix4x4.a12 * h + matrix4x4.a13 * i;
        this.z = matrix4x4.a20 * f + matrix4x4.a21 * g + matrix4x4.a22 * h + matrix4x4.a23 * i;
        this.w = matrix4x4.a30 * f + matrix4x4.a31 * g + matrix4x4.a32 * h + matrix4x4.a33 * i;
        return this;
    }

    public Vector4D multiply(Vector4D vector4D) {
        return multiply(vector4D.getX(), vector4D.getY(), vector4D.getZ(), vector4D.getW());
    }

    public Vector4D divide(Vector4D vector4D) {
        return divide(vector4D.getX(), vector4D.getY(), vector4D.getZ(), vector4D.getW());
    }

    public Vector4D add(Vector4D vector4D) {
        return add(vector4D.getX(), vector4D.getY(), vector4D.getZ(), vector4D.getW());
    }

    public Vector4D subtract(Vector4D vector4D) {
        return subtract(vector4D.getX(), vector4D.getY(), vector4D.getZ(), vector4D.getW());
    }

    public Vector4D multiply(double mul) {
        this.x *= mul;
        this.y *= mul;
        this.z *= mul;
        this.w *= mul;
        return this;
    }

    public Vector4D divide(double div) {
        this.x /= div;
        this.y /= div;
        this.z /= div;
        this.w /= div;
        return this;
    }

    public Vector4D add(double add) {
        this.x += add;
        this.y += add;
        this.z += add;
        this.w += add;
        return this;
    }

    public Vector4D subtract(double sub) {
        this.x -= sub;
        this.y -= sub;
        this.z -= sub;
        this.w -= sub;
        return this;
    }

    public Vector4f toMinecraft() {
        return new Vector4f((float)x, (float)y, (float)z, (float)w);
    }
}
