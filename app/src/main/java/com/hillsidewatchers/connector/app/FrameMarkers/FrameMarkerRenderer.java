/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.hillsidewatchers.connector.app.FrameMarkers;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.hillsidewatchers.connector.utils.ApplicationSession;
import com.hillsidewatchers.connector.utils.Texture;
import com.hillsidewatchers.connector.widget.Ring;
import com.vuforia.Marker;
import com.vuforia.MarkerResult;
import com.vuforia.Matrix34F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vuforia;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 屏幕上出现的模型最终是在这个类中实现的
 */

// The renderer class for the FrameMarkers sample. 
public class FrameMarkerRenderer implements GLSurfaceView.Renderer {
    private static final String LOGTAG = "FrameMarkerRenderer";

    ApplicationSession vuforiaAppSession;
    FrameMarkers mActivity;

    private float startX = 0;
    private float startY = 0;

    public boolean mIsActive = false;

    private Vector<Texture> mTextures;

    Ring ring;

    private float[] mProjectionMatrix = new float[16];

    float centerX;
    float centerY;
    int[] viewport;

    public FrameMarkerRenderer(FrameMarkers activity,
                               ApplicationSession session, float centerX, float centerY) {
        mActivity = activity;
        vuforiaAppSession = session;
        this.centerX = centerX;
        this.centerY = centerY;

    }

    public void setStartCoor(float startX, float startY) {
        this.startX = startX;
        this.startY = startY;
    }

    private float getStartX() {
        return startX;
    }

    private float getStartY() {
        return startY;
    }



    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call function to initialize rendering:
        initRendering();
        //对象的实例化里面涉及到openglES的不能在这个类的构造方法里初始化
        ring = new Ring(0.2f, 0.7f);

        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }

    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        renderFrame();
    }

    void initRendering() {
        Log.d(LOGTAG, "initRendering");
        //画背景色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        // Now generate the OpenGL texture objects and add settings
//        for (Texture t : mTextures) {
//            GLES20.glGenTextures(1, t.mTextureID, 0);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
//                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
//                    GLES20.GL_UNSIGNED_BYTE, t.mData);
//        }

    }

    void renderFrame() {
        //清除颜色缓冲和深度缓冲
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //从vuforia获得状态，并且标志着渲染的开始
        State state = Renderer.getInstance().begin();
        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);//开启深度测试
//        GLES20.glEnable(GLES20.GL_CULL_FACE); //打开背面剪裁
        GLES20.glCullFace(GLES20.GL_BACK);//禁用背面的光照
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        //获取视口数据
        viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
            Matrix34F pose = trackableResult.getPose();
            //追踪效果的视图矩阵
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(pose).getData();

            // Choose the texture based on the target name:
//            int textureIndex = 0;
            MarkerResult markerResult = (MarkerResult) (trackableResult);
            Marker marker = (Marker) markerResult.getTrackable();
//            textureIndex = marker.getMarkerId();
//            Texture thisTexture = mTextures.get(textureIndex);

            // Select which model to draw:
            switch (marker.getMarkerId()) {
                case 0:
//                    qObject1.drawModel(modelViewMatrix, vuforiaAppSession, thisTexture);
                    break;
                case 1:
//                    cObject1.drawModel(modelViewMatrix,vuforiaAppSession,thisTexture);
//                    ring.drawModel(modelViewMatrix, vuforiaAppSession,startX,startY);
                    break;
                case 2:
//                    aObject1.drawModel(modelViewMatrix,vuforiaAppSession,thisTexture);
                    break;
                default:
//                    square.draw(mProjectionMatrix);
//                    ring.draw(modelViewMatrix,vuforiaAppSession);
//                    if (startX != 0)
//                        startX = startX == getStartX() ? 0 : getStartX();
//                    if (startY != 0)
//                        startY = startY == getStartY() ? 0 : getStartY();
//                    ring.drawModel(modelViewMatrix, vuforiaAppSession,mActivity.get, pose);
//                    line.draw(mProjectionMatrix);
                    break;
            }

        }
//        square.draw(mProjectionMatrix);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        Renderer.getInstance().end();
    }


    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;

    }

}
