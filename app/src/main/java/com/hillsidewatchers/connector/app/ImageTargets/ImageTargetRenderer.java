package com.hillsidewatchers.connector.app.ImageTargets;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.hillsidewatchers.connector.utils.ApplicationSession;
import com.hillsidewatchers.connector.utils.LoadingDialogHandler;
import com.hillsidewatchers.connector.utils.Texture;
import com.hillsidewatchers.connector.widget.Ring;
import com.vuforia.Matrix34F;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vuforia;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer {
    private static final String LOGTAG = "ImageTargetRenderer";

    private ApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;

    private Renderer mRenderer;

    boolean mIsActive = false;

    private Ring ring;

    private Vector<Texture> mTextures;//纹理

    private float oldX = 0;
    private float oldY = 0;

    public ImageTargetRenderer(ImageTargets activity,
                               ApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        renderFrame();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering();

        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        vuforiaAppSession.onSurfaceChanged(width, height);
    }


    // Function for initializing the renderer.
    private void initRendering() {
        ring = new Ring(0.3f, 0.7f);

        mRenderer = Renderer.getInstance();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

    }


    // The render function.
    private void renderFrame() {
        //清楚颜色缓冲和深度缓冲
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Set the viewport
        int[] viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera


        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Matrix34F pose = result.getPose();
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(pose);
//            float data[] = new float[16];
//            for (int i = 0; i < 16; i++) {
//                data[i] = modelViewMatrix_Vuforia.getData()[i]*50;
//            }
//            modelViewMatrix_Vuforia.setData(data);
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            float[] touch = mActivity.getTouchStart();
            float startX = touch[0];
            float startY = touch[1];
            if (startX == oldX && startY == oldY) {
                Log.e(LOGTAG, "等于++++++++++++++++++=");
                startX = 0.0f;
                startY = 0.0f;
            }
            ring.drawModel(modelViewMatrix, vuforiaAppSession, startX, startY, pose);
            if (startX != 0 && startY != 0) {
                oldX = startX;
                oldY = startY;
            }

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mRenderer.end();
    }


    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;

    }

}
