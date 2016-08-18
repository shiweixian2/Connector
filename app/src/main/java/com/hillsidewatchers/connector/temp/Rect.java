package com.hillsidewatchers.connector.temp;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.hillsidewatchers.connector.utils.ApplicationSession;
import com.hillsidewatchers.connector.utils.Shader;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by 曾翔钰 on 2016/8/17.
 */
public class Rect {
    private static final String TAG = "Rect";
    int mProgram;//自定义渲染管线程序id
    int aMVPMatrixHandle;//总变换矩阵引用id
    int aPositionHandle;//顶点位置属性引用id
    int aTexCoorHandle;//顶点纹理坐标属性引用id
    String aVertexShader;//顶点着色器
    String aFragmentShader;//片元着色器
    int vCount = 6;
    FloatBuffer aVertexBuffer;
    FloatBuffer aTexCoorBuffer;
    final float UNIT_SIZE = 0.1f;
    float ratio;
    float screenWidth;
    float screenHeight;
    float w_scale;//宽
    float h_scale;//高
    float coor_x;//x轴位置
    float coor_y;//y轴位置
    float sRange = 1;//s纹理坐标范围
    float tRange = 1;//t纹理坐标范围

    public Rect(float w_scale, float h_scale, float coor_x, float coor_y) {
        this.w_scale = w_scale;
        this.h_scale = h_scale;
        this.coor_x = coor_x;
        this.coor_y = coor_y;
        screenWidth = ScreenUtil.screenWidth;
        screenHeight = ScreenUtil.screenHeight;
        ratio = (float) ScreenUtil.screenWidth / (float) ScreenUtil.screenHeight;
        //初始化顶点坐标与着色数据
        initVertexData();
        //初始化着色器
        initShader();
    }

    private void initVertexData() {
        //顶点坐标数据的初始化================begin============================
//        float vertices[]=new float[]
//                {
//                        -ratio*w_scale*UNIT_SIZE,ratio*h_scale*UNIT_SIZE,0,
//                        -ratio*w_scale*UNIT_SIZE,-ratio*h_scale*UNIT_SIZE,0,
//                        ratio*w_scale*UNIT_SIZE,-ratio*h_scale*UNIT_SIZE,0,
//
//                        ratio*w_scale*UNIT_SIZE,-ratio*h_scale*UNIT_SIZE,0,
//                        ratio*w_scale*UNIT_SIZE,ratio*h_scale*UNIT_SIZE,0,
//                        -ratio*w_scale*UNIT_SIZE,ratio*h_scale*UNIT_SIZE,0
//                };
        float x = screenToworldX(w_scale);
        float y = screenToworldY(h_scale);
        float vertices[] = new float[]
                {
                        -x, y, 0,
                        -x, -y, 0,
                        x, -y, 0,

                        x, -y, 0,
                        x, y, 0,
                        -x, y, 0
                };
        aVertexBuffer = (FloatBuffer) bufferUtilFloat(vertices);
        //顶点纹理坐标数据的初始化================begin============================
        float texCoor[] = new float[]//顶点颜色值数组，每个顶点4个色彩值RGBA
                {
                        0, 0,
                        0, tRange,
                        sRange, tRange,
                        sRange, tRange,
                        sRange, 0,
                        0, 0
                };
        aTexCoorBuffer = (FloatBuffer) bufferUtilFloat(texCoor);

    }

    //初始化着色器
    private void initShader() {
        //基于顶点着色器和片元着色器创建程序
        mProgram = ShaderUtil.createProgram(Shader.tempVer, Shader.tempFrag);
        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        aTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        //获取程序中总变换矩阵引用id
        aMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

    }

    public void drawSelf(int texId, ApplicationSession vuforiaAppSession,float[]modelViewMatrix, float x, float y) {
        //制定使用某套shader程序
        GLES20.glUseProgram(mProgram);
        MatrixState.setInitStack();

        float[] mvpMatrix = new float[16];
        Matrix.translateM(modelViewMatrix,0,x,y,0);
        Matrix.scaleM(modelViewMatrix, 0,200,200,200);
        Matrix.multiplyMM(mvpMatrix, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

        //设置沿Z轴正向位移1
//        setCoor(coor_x, coor_y);
        //设置绕y轴旋转
//        MatrixState.rotate(yAngle, 0, 1, 0);
//        //设置绕z轴旋转
//        MatrixState.rotate(zAngle, 0, 0, 1);
//        //设置绕x轴旋转
//        MatrixState.rotate(xAngle, 1, 0, 0);
        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(aMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //为画笔指定顶点位置数据
        GLES20.glVertexAttribPointer
                (
                        aPositionHandle,
                        3,
                        GLES20.GL_FLOAT,
                        false,
                        3 * 4,
                        aVertexBuffer
                );
        //为画笔指定顶点纹理坐标数据
        GLES20.glVertexAttribPointer
                (
                        aTexCoorHandle,
                        2,
                        GLES20.GL_FLOAT,
                        false,
                        2 * 4,
                        aTexCoorBuffer
                );
        //允许顶点位置数据数组
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glEnableVertexAttribArray(aTexCoorHandle);
        //绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
    }

    public Buffer bufferUtilFloat(float[] arr) {
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

    public void setCoor(float x, float y) {

        float trans_x = (x - (screenWidth / 2)) / (float) (screenWidth / 2) * (float) ratio;
        float trans_y = ((screenHeight / 2) - y) / (float) (screenHeight / 2);
        MatrixState.transtate(trans_x, trans_y, 0);

    }

    public float screenToworldX(float x) {
        //这里只是一个宽度，并不是像setCoor方法中有方向
        float world_x = ratio * (float) x / (float) screenWidth;
        return world_x;
    }

    public float screenToworldY(float y) {
        float world_y = y / screenHeight;
        return world_y;
    }

}
