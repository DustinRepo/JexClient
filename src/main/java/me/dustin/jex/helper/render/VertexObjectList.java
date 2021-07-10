package me.dustin.jex.helper.render;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.Arrays;

public class VertexObjectList {

    protected int vertexArrayObject, vertexBufferObject, colorBufferObject;

    private int vertexCount;
    private float[] verticesArray = new float[]{};
    private float[] colorsArray = new float[]{};

    public VertexObjectList() {

    }

    public VertexObjectList vertex(float x, float y, float z) {
        verticesArray = addElement(verticesArray, x);
        verticesArray = addElement(verticesArray, y);
        verticesArray = addElement(verticesArray, z);
        return this;
    }

    public VertexObjectList vertex(Matrix4f matrix4f, float x, float y, float z) {
        Vector4f vector4f = new Vector4f(x, y, z, 1.f);
        vector4f.transform(matrix4f);
        verticesArray = addElement(verticesArray, vector4f.getX());
        verticesArray = addElement(verticesArray, vector4f.getY());
        verticesArray = addElement(verticesArray, vector4f.getZ());
        return this;
    }

    public VertexObjectList color(float red, float green, float blue, float alpha) {
        colorsArray = addElement(colorsArray, red);
        colorsArray = addElement(colorsArray, green);
        colorsArray = addElement(colorsArray, blue);
        colorsArray = addElement(colorsArray, alpha);
        return this;
    }

    public void end() {
        this.vertexArrayObject = GL30.glGenVertexArrays();
        this.vertexBufferObject = GL15.glGenBuffers();

        GL30.glBindVertexArray(vertexArrayObject);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObject);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesArray, GL15.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        this.colorBufferObject = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorBufferObject);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsArray, GL15.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);

        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        vertexCount = (verticesArray.length / 3);
    }

    public void draw() {
        draw(this);
    }

    public static void draw(VertexObjectList vertexObjectList) {
        //RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        GL30.glBindVertexArray(vertexObjectList.vertexArrayObject);
        GL30.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, vertexObjectList.vertexCount);
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);

        GL15.glDeleteBuffers(vertexObjectList.vertexBufferObject);
        GL30.glDeleteVertexArrays(vertexObjectList.vertexArrayObject);
    }

    private float[] addElement(float[] a, float e) {
        a  = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }
}
