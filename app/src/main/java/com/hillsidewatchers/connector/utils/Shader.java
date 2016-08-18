package com.hillsidewatchers.connector.utils;

import android.opengl.GLES20;

/**
 * Created by 炜贤 on 2016/8/3.
 * 着色器
 */
public class Shader {

    public static final String CUBE_MESH_VERTEX_SHADER = " \n" + "\n"
            + "attribute vec4 vertexPosition; \n"
            + "attribute vec4 vertexNormal; \n"
            + "attribute vec2 vertexTexCoord; \n" + "\n"
            + "varying vec2 texCoord; \n" + "varying vec4 normal; \n" + "\n"
            + "uniform mat4 modelViewProjectionMatrix; \n" + "\n"
            + "void main() \n" + "{ \n"
            + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
            + "   normal = vertexNormal; \n" + "   texCoord = vertexTexCoord; \n"
            + "} \n";

    public static final String CUBE_MESH_FRAGMENT_SHADER = " \n" + "\n"
            + "precision mediump float; \n" + " \n" + "varying vec2 texCoord; \n"
            + "varying vec4 normal; \n" + " \n"
            + "uniform sampler2D texSampler2D; \n" + " \n" + "void main() \n"
            + "{ \n" + "   gl_FragColor = texture2D(texSampler2D, texCoord); \n"
            + "} \n";

    //顶点着色器代码
    public static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";
    //片段着色器代码
    public static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    public static final String tempFrag =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord; \n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main()                         \n" +
                    "{           \n" +
                    "   gl_FragColor = texture2D(sTexture, vTextureCoord); \n" +
                    "}              ";

    public static final String tempVer = "uniform mat4 uMVPMatrix; \n" +
            "attribute vec3 aPosition; \n" +
            "attribute vec2 aTexCoor;    \n" +
            "varying vec2 vTextureCoord; \n" +
            "void main()     \n" +
            "{                            \t\t\n" +
            "   gl_Position = uMVPMatrix * vec4(aPosition,1); \n" +
            "   vTextureCoord = aTexCoor;\n" +
            "}                      "
;

    /**
     * 顶点着色器（Vertex Shader）：用来渲染形状顶点的OpenGL ES代码
     *
     * @return 顶点着色器
     */
    public static int getVertexShader() {
        return loadShader(GLES20.GL_VERTEX_SHADER, CUBE_MESH_VERTEX_SHADER);
    }

    public static int getVertexShader(String shaderCode) {
        return loadShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 片段着色器（Fragment Shader）：使用颜色或纹理渲染形状表面的OpenGL ES代码。
     *
     * @return 片段着色器
     */
    public static int getFragmentShader() {
        return loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
    }

    public static int getFragmentShader(String shaderCode) {
        return loadShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }


    /**
     * 加载着色器
     * 我们的程序需要加载两种着色器：vertex shader（顶点着色器）和fragment shader（片段着色器）
     *
     * @param type       要加载的着色器类型
     * @param shaderCode 对应的代码
     * @return 着色器
     */
    private static int loadShader(int type, String shaderCode) {
        //指定类型
        int shader = GLES20.glCreateShader(type);
        // 将源码添加到shader并编译它
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


}
