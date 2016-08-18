package com.hillsidewatchers.connector.widget;

import android.opengl.GLES20;
import android.util.Log;

import com.hillsidewatchers.connector.utils.Shader;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by 曾翔钰 on 2016/7/31.
 */
public class Line {
    private int mProgram;

    private static final String TAG = "Line";
    private float centerX;
    private float centerY;
    //一直起始点
    private float startX = 0.0f;
    private float startY = 0.0f;
    private float endX = 0.0f;
    private float endY = 0.0f;

    private float unit;
    private float width = 100.0f;
    private float[] coorArr = {
            -0.1f, 0.1f, 0f,//左上
            -0.1f, -0.1f, 0f,//左下
            0.1f, -0.1f, 0f,//右下
            0.1f, 0.1f, 0f,//右上

    };
    private FloatBuffer vertexBuffer;

    private final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int vertexCount = coorArr.length / COORDS_PER_VERTEX;
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};
    int flagX;
    int flagY;

    public Line(float centerX, float centerY) {
        vertexBuffer = (FloatBuffer) getVertexBuffer(coorArr);
        unit = centerY;
        this.centerX = centerX;
        this.centerY = centerY;

        initProgram();
    }

    private void initProgram() {
        //获取点着色器
        int vertexShader = Shader.getVertexShader();
        //获取片段着色器
        int fragmentShader = Shader.getFragmentShader();

        // 创建一个空的OpenGL ES Program
        mProgram = GLES20.glCreateProgram();
        // 将vertexShader和fragment shader加入这个program
        GLES20.glAttachShader(mProgram, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 让program可执行
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mMVPMatrix) {
        GLES20.glUseProgram(mProgram);
        int vertexHandle = GLES20.glGetAttribLocation(mProgram,
                "vertexPosition");
        GLES20.glVertexAttribPointer(vertexHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(vertexHandle);

        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "modelViewProjectionMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mMVPMatrix, 0);

        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(vertexHandle);
    }

    /**
     * 设置线段的终点坐标
     * @param endX x
     * @param endY y
     */
    public void setEndDrawCoor(float endX, float endY) {
        this.endX = endX;
        this.endY = endY;
        Log.e("endDraw", "" + endX + ", " + endY);
        double temp = Math.pow((startX - endX), 2) + Math.pow((endY - startY), 2);
        float distanceY = (float) ((Math.abs(endY - startY) * 0.5 * width) / (Math.sqrt(temp)));
        float distanceX = (float) ((Math.abs(endX - startX) * 0.5 * width) / (Math.sqrt(temp)));
        flagX = endX - startX < 0 ? -1 : 1;
        flagY = endY - startY < 0 ? -1 : 1;

        if (flagX * flagY > 0) {
            coorArr[0] = ((this.startX - distanceY) - centerX) / unit;//左上-+
            coorArr[1] = (-(this.startY + distanceX) + centerY) / unit;
            coorArr[3] = ((this.startX + distanceY) - centerX) / unit;//左下--
            coorArr[4] = (-(this.startY - distanceX) + centerY) / unit;

            coorArr[6] = ((this.endX + distanceY) - centerX) / unit;//右下+-
            coorArr[7] = (-(this.endY - distanceX) + centerY) / unit;
            coorArr[9] = ((this.endX - distanceY) - centerX) / unit;//右上++
            coorArr[10] = (-(this.endY + distanceX) + centerY) / unit;
        } else {
            coorArr[0] = ((this.startX - distanceY) - centerX) / unit;//左上-+
            coorArr[1] = (-(this.startY - distanceX) + centerY) / unit;
            coorArr[3] = ((this.startX + distanceY) - centerX) / unit;//左下--
            coorArr[4] = (-(this.startY + distanceX) + centerY) / unit;

            coorArr[6] = ((this.endX + distanceY) - centerX) / unit;//右下+-
            coorArr[7] = (-(this.endY + distanceX) + centerY) / unit;
            coorArr[9] = ((this.endX - distanceY) - centerX) / unit;//右上++
            coorArr[10] = (-(this.endY - distanceX) + centerY) / unit;
        }
        vertexBuffer = (FloatBuffer) getVertexBuffer(coorArr);
    }

    public void setStartDrawCoor(float startX, float startY) {
        this.startX = startX;
        this.startY = startY;
        Log.e("startDraw", "" + startX + ", " + startY);
    }



    public Buffer getVertexBuffer(float[] arr) {
        FloatBuffer mBuffer;
        //先初始化buffer,数组的长度*4,因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        //数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }

    public Buffer bufferUtilShort(short[] arr) {
        ShortBuffer mBuffer;
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                arr.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mBuffer = dlb.asShortBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }
}
