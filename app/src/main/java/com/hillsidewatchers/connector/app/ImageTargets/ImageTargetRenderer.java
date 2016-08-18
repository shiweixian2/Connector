package com.hillsidewatchers.connector.app.ImageTargets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.hillsidewatchers.connector.R;
import com.hillsidewatchers.connector.temp.Rect;
import com.hillsidewatchers.connector.utils.ApplicationSession;
import com.hillsidewatchers.connector.utils.LoadingDialogHandler;
import com.hillsidewatchers.connector.utils.Texture;
import com.hillsidewatchers.connector.widget.Line;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;


// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer {
    private static final String LOGTAG = "ImageTargetRenderer";

    private ApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;

    private Renderer mRenderer;

    boolean mIsActive = false;

    private Ring ring;
    private Line line;

    private float[] modelViewProjectionMatrix = new float[16];
    int count = 0;

    private Vector<Texture> mTextures;//纹理

    private float oldX = 0;
    private float oldY = 0;

    private float lineCurrentStartX = 0;
    private float lineCurrentStartY = 0;
    boolean isDraw = false;

//------------------------------

    private static final String TAG = "SceneRender";
    //    Triangle triangle;
    int textureId;//系统分配的纹理id
    Context context;
    float obj_radio;
    float img_width;
    float img_height;
    float scale = 1.6f;
    float ratio;
    Rect state_txt;
    float state_txt_cx = 250;
    float state_txt_cy = 500;
    Rect state_img;
    float state_img_cx = 250;
    float state_img_cy = 650;
    Rect record_txt;
    float record_txt_cx = 250;
    float record_txt_cy = 850;
    Rect record_img;
    float record_img_cx = 250;
    float record_img_cy = 1200;
    Rect recog_txt;
    float recog_txt_cx = 250;
    float recog_txt_cy = 1350;
    Rect recog_img;
    float recog_img_cx = 250;
    float recog_img_cy = 1500;

    Rect record_num;
    float record_num_cx = 450;
    float record_num_cy = 1500;

    Rect histogram;
    float histogram_cx = 275;
    float histogram_cy = 1050;

    Rect histogram1;
    float histogram1_cx = 425;
    float histogram1_cy = 1000;

    Rect histogram2;
    float histogram2_cx = 575;
    float histogram2_cy = 1025;
    Rect histogram3;
    float histogram3_cx = 725;
    float histogram3_cy = 1100;
    //所有的纹理
    int[] res = {R.drawable.state, R.drawable.open, R.drawable.record_txt,
            R.drawable.recog_txt, R.drawable.person, R.drawable.record_img, R.drawable.blue, R.drawable.num};
    int[] textures = new int[res.length];
//-----------------


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
        line = new Line(ImageTargets.screenWidth / 2, ImageTargets.screenHeight / 2);
        state_txt = createObject(68f, state_txt_cx, state_txt_cy, R.drawable.state);
        state_img = createObject(120f, state_img_cx, state_img_cy, R.drawable.open);
        record_txt = createObject(68f, record_txt_cx, record_txt_cy, R.drawable.record_txt);
        record_img = createObject(95f, record_img_cx, record_img_cy, R.drawable.record_img);
        recog_txt = createObject(68f, recog_txt_cx, recog_txt_cy, R.drawable.recog_txt);
        recog_img = createObject(155, recog_img_cx, recog_img_cy, R.drawable.person);
        record_num = createObject(85, record_num_cx, record_num_cy, R.drawable.num);

        histogram = createHistogram(85, 275.0f, 1150, 2.0f);
        histogram1 = createHistogram(85, 405, 1150, 2.2f);
        histogram2 = createHistogram(85, 540, 1150, 2.58f);
        histogram3 = createHistogram(85, 675, 1150, 0.9f);
        mRenderer = Renderer.getInstance();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        setBgTransparent();
        //初始化纹理
        initTexture(res);
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

        float[] centerCoordinates = null;
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Matrix34F pose = result.getPose();
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(pose);
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

//            float[] touch = mActivity.getTouchStart();
//            float startX = touch[0];
//            float startY = touch[1];
//            if (startX == oldX && startY == oldY) {
//                Log.e(LOGTAG, "等于++++++++++++++++++=");
//                startX = 0.0f;
//                startY = 0.0f;
//            }

            Rect rects[] = {state_txt,state_img,record_txt,recog_txt,recog_img,record_img,histogram,histogram1,
                    histogram2,histogram3,record_num};
            int[][] translate = {{-30,50},{-30,20},{-30,0},{-30,-30},{-30,-70}};


            drawRect(rects,modelViewMatrix,translate);

//            float modelViewMatrixOne[] = copyMatrix(modelViewMatrix);
//            float modelViewMatrixTwo[] = copyMatrix(modelViewMatrix);
//            //绘制纹理矩形
//            state_txt.drawSelf(textures[0], vuforiaAppSession, modelViewMatrixOne, 50, 40);
//            state_img.drawSelf(textures[1], vuforiaAppSession, modelViewMatrixTwo, 100, 20);
//            record_txt.drawSelf(textures[2],mvpMatrix);
//            recog_txt.drawSelf(textures[3],mvpMatrix);
//            recog_img.drawSelf(textures[4],mvpMatrix);
//            record_img.drawSelf(textures[5],mvpMatrix);
//            histogram.drawSelf(textures[6],mvpMatrix);
//            histogram1.drawSelf(textures[6],mvpMatrix);
//            histogram2.drawSelf(textures[6],mvpMatrix);
//            histogram3.drawSelf(textures[6],mvpMatrix);
//            record_num.drawSelf(textures[7],mvpMatrix);


//            float modelViewMatrixOne[] = copyMatrix(modelViewMatrix);
//            Matrix.translateM(modelViewMatrixOne, 0, 30,
//                    -200, 0.f);
//            Matrix.scaleM(modelViewMatrixOne, 0, 100, 100, 100);
//            float modelViewMatrixTwo[] = copyMatrix(modelViewMatrix);
//            Matrix.translateM(modelViewMatrixTwo, 0, 50,
//                    150, 0.f);
//            Matrix.scaleM(modelViewMatrixTwo, 0, 100, 100, 100);
//            float modelViewMatrixThree[] = copyMatrix(modelViewMatrix);
//            Matrix.translateM(modelViewMatrixThree, 0, -100,
//                    0, 0.f);
//            Matrix.scaleM(modelViewMatrixThree, 0, 100, 100, 100);
//            float modelViewMatrixs[][] = {modelViewMatrixOne, modelViewMatrixTwo, modelViewMatrixThree};
//            float translates[][] = {{30, -200}, {50, 150}, {-100, 0}};
//            int scale[] = {100, 100, 100};
//            //画圆圈
//            drawRings(modelViewMatrixs, mRenderer, pose, translates, scale, startX, startY);

//
//            if (startX != 0 && startY != 0) {
//                oldX = startX;
//                oldY = startY;
//            }

        }

//        if (centerCoordinates != null && isDraw)
//            line.setStartDrawCoor(centerCoordinates[0], centerCoordinates[1]);
//        //画线
//        line.setEndDrawCoor(mActivity.getTouchEnd()[0], mActivity.getTouchEnd()[1]);
//        if (count != 0)
//            line.draw(modelViewProjectionMatrix);
//        Log.e(LOGTAG, "画线");


        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mRenderer.end();

//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }


    private void drawRect(Rect[] rects,float[] modelViewMatrix,int[][] translate){
        for (int i = 0; i < 5; i++) {
            float tempModelViewMatrix[] = copyMatrix(modelViewMatrix);
            //绘制纹理矩形
            rects[i].drawSelf(textures[i], vuforiaAppSession, tempModelViewMatrix,translate[i][0],translate[i][1]);
        }
    }

    /**
     * 设置背景透明
     */
    public void setBgTransparent() {
//        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public Rect createHistogram(float scale, float bottomX, float bottomY, float obj_radio) {
        Rect obj;
        this.obj_radio = obj_radio;
        float obj_centerX = bottomX + scale / 2;
        float obj_centerY = bottomY - scale * obj_radio / 2;
        float w_scale = scale;
        float h_scale = scale * obj_radio;
        obj = new Rect(w_scale, h_scale, obj_centerX, obj_centerY);
        return obj;

    }

    /**
     * @param scale   大小，可以是宽也可以是高，取决于想要宽不变还是高不变，用图片的宽高比 * scale可以得到另一边
     * @param coor_x
     * @param coor_y
     * @param img_res 贴图资源
     * @return
     */
    public Rect createObject(float scale, float coor_x, float coor_y, int img_res) {
        Rect obj;
        obj_radio = getImg_radio(img_res);
        coor_x = coor_x + (float) ((float) scale * (float) obj_radio / 2);
        Log.e(TAG, "img_radio:" + obj_radio);
        //传入长、宽、位置x、y可以得到一个矩形，这个矩形的大小正好和贴图的长宽比是一样的，不会发生图片变形
        obj = new Rect(obj_radio * scale, scale, coor_x, coor_y);
        return obj;
    }

    /**
     * 获得图像的宽高比率、width/height
     *
     * @param res
     * @return
     */
    public float getImg_radio(int res) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mActivity.getResources(), res, opts);
        opts.inSampleSize = 1;
        opts.inJustDecodeBounds = false;
        img_width = opts.outWidth;
        img_height = opts.outHeight;

        float radio = (float) img_width / (float) img_height;
        return radio;
    }


    private void drawRings(float[][] modelViewMatrixs, Renderer renderer, Matrix34F pose, float translates[][], int[] scale, float startX, float startY) {
        for (int i = 0; i < modelViewMatrixs.length; i++) {
            float ringData[] = ring.drawModel(modelViewMatrixs[i], renderer, vuforiaAppSession, pose, scale[i], i);
            float ringCenterX = ringData[0] + translates[i][0] * 2f;
            float ringCenterY = ringData[1] - translates[i][1] * 2f;
            float ringRadius = ringData[2];
            if (startX != 0 || startY != 0) {
                Log.e("手指触碰点", "" + startX + "--" + startY);
                if (startX <= ringCenterX + ringRadius && startX >= ringCenterX - ringRadius && startY <= ringCenterY + ringRadius && startY >= ringCenterY - ringRadius) {
                    Log.e("手指点", "在图形 " + i + " 中");
                    Matrix.multiplyMM(modelViewProjectionMatrix, 0, vuforiaAppSession
                            .getProjectionMatrix().getData(), 0, modelViewMatrixs[i], 0);
                    count++;
                    if (startX != 0)
                        lineCurrentStartX = ringCenterX;
                    if (startY != 0)
                        lineCurrentStartY = ringCenterY;
                    line.setStartDrawCoor(lineCurrentStartX, lineCurrentStartY);
                    isDraw = true;

                }
            }
//            return new float[]{ringCenterX, ringCenterY};
        }
//        return null;
    }


    private float[] copyMatrix(float[] modelViewMatrix) {
        float modelViewMatrixOne[] = new float[16];
        for (int i = 0; i < modelViewMatrix.length; i++) {
            modelViewMatrixOne[i] = modelViewMatrix[i];
        }
        return modelViewMatrixOne;
    }

    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;

    }

    public void initTexture(int[] res)//textureId
    {
        //生成纹理ID

        GLES20.glGenTextures
                (
                        res.length,          //产生的纹理id的数量
                        textures,   //纹理id的数组
                        0           //偏移量
                );
        for (int i = 0; i < res.length; i++) {
            textureId = textures[i];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //通过输入流加载图片===============begin===================
            InputStream is = mActivity.getResources().openRawResource(res[i]);
            Bitmap bitmapTmp;
            try {

                bitmapTmp = BitmapFactory.decodeStream(is);

            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //通过输入流加载图片===============end=====================

            //实际加载纹理
            GLUtils.texImage2D
                    (
                            GLES20.GL_TEXTURE_2D,   //纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
                            0,                      //纹理的层次，0表示基本图像层，可以理解为直接贴图
                            bitmapTmp,              //纹理图像
                            0                      //纹理边框尺寸
                    );
            bitmapTmp.recycle();          //纹理加载成功后释放图片
        }

    }

}
