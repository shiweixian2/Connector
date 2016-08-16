package com.hillsidewatchers.connector.widget;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.hillsidewatchers.connector.app.ImageTargets.ImageTargets;
import com.hillsidewatchers.connector.utils.ApplicationSession;
import com.hillsidewatchers.connector.utils.SampleUtils;
import com.hillsidewatchers.connector.utils.Shader;
import com.vuforia.CameraCalibration;
import com.vuforia.CameraDevice;
import com.vuforia.Matrix34F;
import com.vuforia.Renderer;
import com.vuforia.Tool;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by 曾翔钰 on 2016/7/31.
 * 圆环
 */
public class Ring {
    private static final String TAG = "Ring";
    private int mProgram;

    //每个点坐标数
    private static final int COORDS_PER_VERTEX = 2;
    //总坐标数(顶点数*每个顶点的坐标数）
    private final int COORDS_COUNT = 5;
    private int mPositionHandle;
    private int mColorHandle;
    //顶点个数
    private int vertexCount;
    //每个定点的字节数
    private int byte_per_vertex = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;
    //外、内环颜色数组
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 0.5f};
    float color2[] = {0.0f, 1f, 0.0f, 1.0f};

    //存储坐标的链表
    List<Float> coordsList = new ArrayList<>();
    List in_list = new ArrayList();

    float radius;
    float in_radius;
    float[] ringCoordsArr;
    //内外圆之间的比率
    float rate;

    static private float scale = 80.0f;
    static private float translate = 0.0f;
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int mvpMatrixHandle = 0;
    //    private int texSampler2DHandle = 0;


    public Ring(float radius, float rate) {
        this.radius = radius;
        this.rate = rate;
        in_radius = rate * radius;
        for (int i = 0; i < COORDS_COUNT; i++) {
            //将外圆坐标加入链表中
            float x = (float) (radius * Math.cos((2 * Math.PI * i / COORDS_COUNT)));
            float y = (float) (radius * Math.sin((2 * Math.PI * i / COORDS_COUNT)));
            coordsList.add(x);
            coordsList.add(y);
            //将内圆坐标加入链表中
            float in_x = (float) (in_radius * Math.cos((2 * Math.PI * i) / COORDS_COUNT));
            float in_y = (float) (in_radius * Math.sin((2 * Math.PI * i) / COORDS_COUNT));
            coordsList.add(in_x);
            coordsList.add(in_y);
        }
        //加入起始点
        float x = (float) (radius * Math.cos((2 * Math.PI * 0 / COORDS_COUNT)));
        float y = (float) (radius * Math.sin((2 * Math.PI * 0 / COORDS_COUNT)));
        coordsList.add(x);
        coordsList.add(y);
        float in_x = (float) (in_radius * Math.cos((2 * Math.PI * 0) / COORDS_COUNT));
        float in_y = (float) (in_radius * Math.sin((2 * Math.PI * 0) / COORDS_COUNT));
        coordsList.add(in_x);
        coordsList.add(in_y);
        //将链表内容转移到数组
        ringCoordsArr = new float[coordsList.size()];
        for (int i = 0; i < coordsList.size(); i++) {
            ringCoordsArr[i] = coordsList.get(i);
            Log.e("coordiante", "" + ringCoordsArr[i]);
        }

        vertexBuffer = (FloatBuffer) getVertexBuffer(ringCoordsArr);
//        vertexBuffer2 = (FloatBuffer) getVertexBuffer(in_ringcoorAr);
        vertexCount = coordsList.size() / COORDS_PER_VERTEX;

        //获取点着色器
        int vertexShader = Shader.getVertexShader();
        //获取片段着色器
        int fragmentShader = Shader.getFragmentShader();

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                Shader.CUBE_MESH_VERTEX_SHADER,Shader.fragmentShaderCode);
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");

        mColorHandle = GLES20.glGetUniformLocation(shaderProgramID, "vColor");

//        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
//                "texSampler2D");
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }


    public void drawModel(float[] modelViewMatrix, ApplicationSession vuforiaAppSession, float startX, float startY, Matrix34F pose) {
        float[] mvpMatrix = new float[16];
//        //平移
        Matrix.translateM(modelViewMatrix, 0, translate,
                translate, 0.f);
//        //缩放
        Matrix.scaleM(modelViewMatrix, 0, scale, scale,
                scale);
        //合并矩阵
        Matrix.multiplyMM(mvpMatrix, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
        //获取指向着色器的程式（program)
        GLES20.glUseProgram(shaderProgramID);

        GLES20.glVertexAttribPointer(vertexHandle, 2, GLES20.GL_FLOAT,
                false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(vertexHandle);

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                mvpMatrix, 0);

//        GLES20.glUniform1i(texSampler2DHandle, 0);

        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(vertexHandle);
        SampleUtils.checkGLError("FrameMarkers render frame");

        //--将图像的坐标转为屏幕坐标
        int screenWidth = ImageTargets.screenWidth;
        int screenHeight = ImageTargets.screenHeight;
        VideoMode videoMode = CameraDevice.getInstance().getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();
        int xOffset = (screenWidth - config.getSize().getData()[0]) / 2 + config.getPosition().getData()[0];
        int yOffset = (screenHeight - config.getSize().getData()[1]) / 2 + config.getPosition().getData()[1];

        for (int i = 0; i < ringCoordsArr.length; i += 2) {
            Log.e("Ring", "一个坐标");
            CameraCalibration calibration = CameraDevice.getInstance().getCameraCalibration();
            float temp[] = new float[]{ringCoordsArr[i], ringCoordsArr[i + 1], 0.0f};
            Vec2F vec2F = Tool.projectPoint(calibration, pose, new Vec3F(temp));
            float rotatedX = videoMode.getHeight() - vec2F.getData()[1];
            float rotatedY = vec2F.getData()[0];
            float xxx = rotatedX * config.getSize().getData()[0] / videoMode.getHeight() + xOffset;
            float yyy = rotatedY * config.getSize().getData()[1] / videoMode.getWidth() + yOffset;
            Log.e("Ring", "" + xxx + "++" + yyy);
        }

        //手指
        if (startX != 0 || startY != 0) {
            Log.e("手指触碰点", "" + startX + "--" + startY);
        }


    }

    /**
     * openGL ES采用列向量,应该是Q=MP，即矩阵*坐标
     *
     * @param left  M
     * @param right P
     * @return
     */
    public float[] getResult(float left[], float right[]) {
        float result[] = new float[4];
        for (int i = 0; i < 4; i++) {
            float temp = 0;
            for (int j = 0; j < 4; j++) {
                temp += left[i + j * 4] * right[j];
            }
            result[i] = temp;
        }
        return result;
    }

    /**
     * 为存储顶点的数组初始化空间
     *
     * @param arr 顶点数组
     * @return FloatBuffer
     */
    public Buffer getVertexBuffer(float[] arr) {
        FloatBuffer mBuffer;
        //先初始化Bytebuffer,一份float占4个字节，因此乘以4
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        //数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        mBuffer = qbb.asFloatBuffer();
        //将数组放进缓冲区
        mBuffer.put(arr);
        //重置指针为第一个位置
        mBuffer.position(0);
        return mBuffer;
    }
}
