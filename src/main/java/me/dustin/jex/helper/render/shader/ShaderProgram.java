package me.dustin.jex.helper.render.shader;

import me.dustin.jex.JexClient;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL20.*;

public abstract class ShaderProgram {

	private final String shaderName;
	private final int shaderProgram;
	private final ArrayList<ShaderUniform> uniforms = new ArrayList<>();
	private Runnable updateUniforms;

	public ShaderProgram(String shaderName) {
		this.shaderName = shaderName;
		this.shaderProgram = glCreateProgram();
		String vCode = readShader("/assets/jex/shaders/%s.vsh".formatted(shaderName));
		String fCode = readShader("/assets/jex/shaders/%s.fsh".formatted(shaderName));
		createProgram(vCode, fCode);
	}

	public ShaderProgram(String namespace, String shaderName) {
		this.shaderName = shaderName;
		this.shaderProgram = glCreateProgram();
		String vCode = readShader("/assets/%s/shaders/%s.vsh".formatted(namespace, shaderName));
		String fCode = readShader("/assets/%s/shaders/%s.fsh".formatted(namespace, shaderName));
		createProgram(vCode, fCode);
	}

	public void bind() {
		glUseProgram(shaderProgram);
		updateUniforms();
		if (this.updateUniforms != null)
			this.updateUniforms.run();
	}

	public void detach() {
		glUseProgram(0);
	}

	public abstract void updateUniforms();

	public ShaderUniform addUniform(String name) {
		int i = glGetUniformLocation(shaderProgram, name);
		if (i < 0) {
			throw new NullPointerException("Could not find uniform " + name + " in shader " + shaderName);
		}
		ShaderUniform uniform = new ShaderUniform(name, i);
		uniforms.add(uniform);
		return uniform;
	}

	public ShaderUniform getUniform(String name) {
		for (ShaderUniform uniform : uniforms) {
			if (uniform.getName().equals(name)) {
				return uniform;
			}
		}
		return new ShaderUniform(name, -1);
	}

	public void bindAttribute(String name, int index) {
		glBindAttribLocation(shaderProgram, index, name);
	}

	public void createProgram(String vertex, String frag) {
		int vertexID = loadShader(vertex, GL_VERTEX_SHADER);
		int fragID = loadShader(frag, GL_FRAGMENT_SHADER);
		glAttachShader(shaderProgram, vertexID);
		glAttachShader(shaderProgram, fragID);
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);
		int errorCheckValue = glGetError();
		if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
			JexClient.INSTANCE.getLogger().info(this.shaderName + " Shader failed to compile!");
			JexClient.INSTANCE.getLogger().info(glGetProgramInfoLog(shaderProgram, 2048));
			System.exit(-1);
		}
		// Validating here seems to break macs
		/*
		 * if(glGetProgrami(shaderProgram, GL_VALIDATE_STATUS) == GL_FALSE) {
		 * JexClient.INSTANCE.getLogger().info(this.shaderName +
		 * " Shader failed to compile!");
		 * JexClient.INSTANCE.getLogger().info(glGetProgramInfoLog(shaderProgram,
		 * 2048)); System.exit(-1); }
		 */
		if (errorCheckValue != GL_NO_ERROR) {
			JexClient.INSTANCE.getLogger().info(this.shaderName + " Could not create shader " + this.shaderName);
			JexClient.INSTANCE.getLogger().info(glGetProgramInfoLog(shaderProgram, glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH)));
			System.exit(-1);
		}
	}

	private int loadShader(String source, int type) {
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, source);
		glCompileShader(shaderID);

		if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
			JexClient.INSTANCE.getLogger().info(this.shaderName + " Shader failed to compile!");
			JexClient.INSTANCE.getLogger().info(glGetShaderInfoLog(shaderID, 2048));
			System.exit(-1);
		}
		return shaderID;
	}

	private String readShader(String fileLoc) {
		try {
			InputStream in = getClass().getResourceAsStream(fileLoc);
			if (in == null)
			   return "InputStream is null";
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String inString;
			while ((inString = reader.readLine()) != null) {
				sb.append(inString);
				sb.append("\n");
			}
			in.close();
			reader.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error";
		}
	}

	public void setUpdateUniforms(Runnable updateUniforms) {
		this.updateUniforms = updateUniforms;
	}
}
