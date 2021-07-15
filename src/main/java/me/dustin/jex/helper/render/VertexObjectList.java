package me.dustin.jex.helper.render;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.math.vector.Vector2D;
import me.dustin.jex.helper.math.vector.Vector3D;
import me.dustin.jex.helper.math.vector.Vector3I;
import me.dustin.jex.helper.math.vector.Vector4D;
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

public class VertexObjectList {

    private static VertexObjectList vertexObjectList = new VertexObjectList();

    protected int vertexArrayObject, vertexBufferObject, indexBufferObject, colorBufferObject, texBufferObject;

    private DrawMode drawMode;
    private Format format;
    private int vertexCount;
    private final ArrayList<Vector3D> verticesArray = new ArrayList<>();
    private final ArrayList<Vector4D> colorsArray = new ArrayList<>();
    private final ArrayList<Vector2D> texArray = new ArrayList<>();
    private final ArrayList<Vector3I> indicesArray = new ArrayList<>();
    private boolean building;

    public VertexObjectList() {
    }

    public static VertexObjectList getMain() {
        return vertexObjectList;
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
        Vector3I vector3I = new Vector3I(index1, index2, index3);
        indicesArray.add(vector3I);
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

    public VertexObjectList color(int red, int green, int blue, int alpha) {
        Vector4D vector4D = new Vector4D(red/255.f, green/255.f, blue/255.f, alpha/255.f);
        colorsArray.add(vector4D);
        return this;
    }

    public VertexObjectList tex(Vector2D vector2D) {
        texArray.add(vector2D);
        return this;
    }

    public VertexObjectList tex(float u, float v) {
        Vector2D vector2D = new Vector2D(u,v);
        return tex(vector2D);
    }

    public void begin(DrawMode drawMode, Format format) {
        if (building) {
            JexClient.INSTANCE.getLogger().info("Already building! You must use .end() and .draw() before using again!");
            building = false;
            return;
        }
        this.drawMode = drawMode;
        this.format = format;
        this.vertexArrayObject = GL30.glGenVertexArrays();
        this.vertexBufferObject = GL15.glGenBuffers();
        this.colorBufferObject = GL15.glGenBuffers();
        this.indexBufferObject = GL15.glGenBuffers();
        this.texBufferObject = GL15.glGenBuffers();
        building = true;
    }

    public void end() {
        GL30.glBindVertexArray(vertexArrayObject);
        if (!indicesArray.isEmpty())
            bindIndices();
        switch (format) {
            case POS_COLOR_TEX:
                storeAttribute(vertexBufferObject, 0, 3, toFloatBufferVec3D(verticesArray));
                storeAttribute(colorBufferObject, 1, 4, toFloatBufferVec4D(colorsArray));
                storeAttribute(texBufferObject, 2, 2, toFloatBufferVec2D(texArray));
            break;
            case POS_COLOR:
                storeAttribute(vertexBufferObject, 0, 3, toFloatBufferVec3D(verticesArray));
                storeAttribute(colorBufferObject, 1, 4, toFloatBufferVec4D(colorsArray));
            break;

        }
        GL30.glBindVertexArray(0);
        vertexCount = indicesArray.isEmpty() ? verticesArray.size() : indicesArray.size() * 3;
        verticesArray.clear();
        colorsArray.clear();
        texArray.clear();
    }

    public void draw() {
        draw(this);
    }

    public static void draw(VertexObjectList vertexObjectList) {
        //RenderSystem.clear(GL30.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        GL30.glBindVertexArray(vertexObjectList.vertexArrayObject);
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        if (!vertexObjectList.indicesArray.isEmpty())
            GL11.glDrawElements(vertexObjectList.getGLDrawMode(), vertexObjectList.vertexCount, GL11.GL_UNSIGNED_INT, 0);
        else
            GL30.glDrawArrays(vertexObjectList.getGLDrawMode(), 0, vertexObjectList.vertexCount);
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
        vertexObjectList.deleteBuffers();
        vertexObjectList.building = false;
        vertexObjectList.indicesArray.clear();
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

    private void deleteBuffers() {
        GL15.glDeleteBuffers(vertexObjectList.vertexBufferObject);
        GL30.glDeleteBuffers(vertexObjectList.indexBufferObject);
        GL30.glDeleteBuffers(vertexObjectList.colorBufferObject);
        GL30.glDeleteBuffers(vertexObjectList.texBufferObject);
        GL30.glDeleteVertexArrays(vertexObjectList.vertexArrayObject);
        GL30.glDeleteVertexArrays(vertexObjectList.vertexArrayObject);
    }

    private IntBuffer toIntBuffer(ArrayList<Vector3I> array) {
        IntBuffer intBuffer = MemoryUtil.memAllocInt(array.size() * 3);
        int[] ints = new int[array.size() * 3];
        for (int i = 0; i < array.size(); i++) {
            Vector3I vector3I = array.get(i);
            ints[i*3] = vector3I.getX();
            ints[i*3+1] = vector3I.getY();
            ints[i*3+2] = vector3I.getZ();
        }
        return intBuffer.put(ints).flip();
    }

    private FloatBuffer toFloatBuffer(float[] array) {
        FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(array.length);
        return floatBuffer.put(array).flip();
    }

    private FloatBuffer toFloatBufferVec2D(ArrayList<Vector2D> array) {
        float[] floats = new float[array.size() * 2];
        for (int i = 0; i < array.size(); i++) {
            Vector2D vector3D = array.get(i);
            floats[i*2] = (float)vector3D.getX();
            floats[i*2+1] = (float)vector3D.getY();
        }
        return toFloatBuffer(floats);
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

    private int getGLDrawMode() {
        switch(drawMode) {
            case QUAD -> {return indicesArray.isEmpty() ? GL11.GL_TRIANGLE_STRIP : GL11.GL_TRIANGLES;}
            case LINE -> {return GL11.GL_LINES;}
        }
        return GL11.GL_TRIANGLES;
    }

    public enum DrawMode {
        QUAD, LINE
    }

    public enum Format {
        POS_COLOR, POS_COLOR_TEX
    }
}
