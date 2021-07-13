package me.dustin.jex.helper.render;

import me.dustin.jex.helper.math.Vector3D;
import me.dustin.jex.helper.math.Vector4D;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class VertexObjectList {

    protected int vertexArrayObject, vertexBufferObject, indexBufferObject, colorBufferObject;

    private DrawMode drawMode;
    private int vertexCount;
    private ArrayList<Vector3D> verticesArray = new ArrayList<>();
    private ArrayList<Vector4D> colorsArray = new ArrayList<>();
    //private float[] verticesArray = new float[]{};
    private int[] indicesArray = new int[]{};
    //private float[] colorsArray = new float[]{};

    public VertexObjectList(DrawMode drawMode) {
        this.drawMode = drawMode;
    }

    public VertexObjectList vertex(Vector3D vector3D) {
        verticesArray.add(vector3D);
        return this;
    }

    public VertexObjectList vertex(float x, float y, float z) {
        Vector3D vector3D = new Vector3D(x, y, z);
        verticesArray.add(vector3D);
        return this;
    }

    public VertexObjectList vertex(Matrix4f matrix4f, float x, float y, float z) {
        Vector3D vector3D = new Vector3D(x, y, z).transform(matrix4f);
        vertex(vector3D);
        return this;
    }

    public VertexObjectList index(int index1, int index2, int index3) {
        indicesArray = addElement(indicesArray, index1);
        indicesArray = addElement(indicesArray, index2);
        indicesArray = addElement(indicesArray, index3);
        return this;
    }

    public VertexObjectList color(Color color) {
        float red = color.getRed() / 255.f;
        float green = color.getGreen() / 255.f;
        float blue = color.getBlue() / 255.f;
        float alpha = color.getAlpha() / 255.f;
        return color(red, green, blue, alpha);
    }

    public VertexObjectList color(float red, float green, float blue, float alpha) {
        Vector4D vector4D = new Vector4D(red, green, blue, alpha);
        colorsArray.add(vector4D);
        return this;
    }

    public void end() {
        this.vertexArrayObject = GL30.glGenVertexArrays();
        this.vertexBufferObject = GL15.glGenBuffers();
        this.colorBufferObject = GL15.glGenBuffers();
        this.indexBufferObject = GL15.glGenBuffers();

        GL30.glBindVertexArray(vertexArrayObject);
        if (indicesArray.length != 0)
            bindIndices();
        storeAttribute(vertexBufferObject, 0, 3, toFloatBufferVec3D(verticesArray));
        storeAttribute(colorBufferObject, 1, 4, toFloatBufferVec4D(colorsArray));

        GL30.glBindVertexArray(0);
        vertexCount = indicesArray.length == 0 ? verticesArray.size() : indicesArray.length;
        verticesArray.clear();
        colorsArray.clear();
    }

    public void draw() {
        draw(this);
    }

    public static void draw(VertexObjectList vertexObjectList) {
        //RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        GL30.glBindVertexArray(vertexObjectList.vertexArrayObject);
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        if (vertexObjectList.indicesArray.length != 0)
            GL11.glDrawElements(vertexObjectList.getGLDrawMode(), vertexObjectList.vertexCount, GL11.GL_UNSIGNED_INT, 0);
        else
            GL30.glDrawArrays(vertexObjectList.getGLDrawMode(), 0, vertexObjectList.vertexCount);
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);

        GL15.glDeleteBuffers(vertexObjectList.vertexBufferObject);
        GL30.glDeleteBuffers(vertexObjectList.indexBufferObject);
        GL30.glDeleteVertexArrays(vertexObjectList.vertexArrayObject);
    }

    private void bindIndices() {
        IntBuffer buffer = toIntBuffer(indicesArray);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private void storeAttribute(int id, int index, int size, FloatBuffer data) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(index, size, GL11.GL_FLOAT, false, 0, 0);
    }

    private IntBuffer toIntBuffer(int[] array) {
        IntBuffer intBuffer = MemoryUtil.memAllocInt(array.length);
        return intBuffer.put(array).flip();
    }

    private FloatBuffer toFloatBuffer(float[] array) {
        FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(array.length);
        return floatBuffer.put(array).flip();
    }

    private FloatBuffer toFloatBufferVec3D(ArrayList<Vector3D> array) {
        float[] floats = new float[array.size() * 3];
        for (int i = 0; i < array.size(); i++) {
            Vector3D vector3D = array.get(i);
            floats[i*3] = (float)vector3D.getX();
            floats[i*3+1] = (float)vector3D.getY();
            floats[i*3+2] = (float)vector3D.getZ();
        }
        return toFloatBuffer(floats);
    }

    private FloatBuffer toFloatBufferVec4D(ArrayList<Vector4D> array) {
        float[] floats = new float[array.size() * 4];
        for (int i = 0; i < array.size(); i++) {
            Vector4D vector4D = array.get(i);
            floats[i*4] = (float)vector4D.getX();
            floats[i*4+1] = (float)vector4D.getY();
            floats[i*4+2] = (float)vector4D.getZ();
            floats[i*4+3] = (float)vector4D.getW();
        }
        return toFloatBuffer(floats);
    }

    private int[] addElement(int[] a, int e) {
        a  = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }

    private float[] addElement(float[] a, float e) {
        a  = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }

    private int getGLDrawMode() {
        switch(drawMode) {
            case QUAD -> {return indicesArray.length == 0 ? GL11.GL_TRIANGLE_STRIP : GL11.GL_TRIANGLES;}
            case LINE -> {return GL11.GL_LINES;}
        }
        return GL11.GL_TRIANGLES;
    }

    public enum DrawMode {
        QUAD, LINE
    }
}
