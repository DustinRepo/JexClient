package me.dustin.jex.helper.math.vector;

import net.minecraft.world.phys.Vec2;

public class Vector2D {

	public double x, y;

	public Vector2D() {
		this.x = 0;
		this.y = 0;
	}

	public Vector2D(Vec2 vec2f) {
		this.x = vec2f.x;
		this.y = vec2f.y;
	}

	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
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

	public Vector2D multiply(double mulX, double mulY) {
		this.x *= mulX;
		this.y *= mulY;
		return this;
	}

	public Vector2D divide(double divX, double divY) {
		this.x /= divX;
		this.y /= divY;
		return this;
	}

	public Vector2D add(double addX, double addY) {
		this.x += addX;
		this.y += addY;
		return this;
	}

	public Vector2D subtract(double subX, double subY) {
		this.x -= subX;
		this.y -= subY;
		return this;
	}

	public Vector2D multiply(Vector2D vector2D) {
		return multiply(vector2D.getX(), vector2D.getY());
	}

	public Vector2D divide(Vector2D vector2D) {
		return divide(vector2D.getX(), vector2D.getY());
	}

	public Vector2D add(Vector2D vector2D) {
		return add(vector2D.getX(), vector2D.getY());
	}

	public Vector2D subtract(Vector2D vector2D) {
		return subtract(vector2D.getX(), vector2D.getY());
	}

	public Vector2D multiply(double mul) {
		this.x *= mul;
		this.y *= mul;
		return this;
	}

	public Vector2D divide(double div) {
		this.x /= div;
		this.y /= div;
		return this;
	}

	public Vector2D add(double add) {
		this.x += add;
		this.y += add;
		return this;
	}

	public Vector2D subtract(double sub) {
		this.x -= sub;
		this.y -= sub;
		return this;
	}

	public Vec2 toMinecraft() {
		return new Vec2((float) x, (float) y);
	}
}
