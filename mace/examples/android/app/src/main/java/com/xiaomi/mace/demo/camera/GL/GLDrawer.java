package com.xiaomi.mace.demo.camera.GL;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.xiaomi.mace.demo.camera.Engage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dhb on 2018/10/29.
 */

public class GLDrawer {

    private final String TAG = "GLDrawer";

    private static final String CAMERA_INPUT_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	textureCoordinate = inputTextureCoordinate.xy;\n" +
            "	gl_Position = position;\n" +
            "}";

    private static final String CAMERA_INPUT_FRAGMENT_SHADER_OES = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public static final String CAMERA_INPUT_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer, mTextureBuffer, mGLSaveTextureBuffer;
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private ShortBuffer drawListBuffer;

    //image width and height
    private int width;
    private int height;

    //surface width and height
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private Engage engage;
    private ByteBuffer byteBuffer;
    private ScaleType mScaleType;

    private int[] mFrameBuffers;
    private int[] mFrameBufferTextures;

    private final static String PROGRAM_ID = "program";
    private final static String POSITION_COORDINATE = "position";
    private final static String TEXTURE_UNIFORM = "inputImageTexture";
    private final static String TEXTURE_COORDINATE = "inputTextureCoordinate";

    private boolean mIsInitialized;

    private ArrayList<HashMap<String, Integer>> mArrayPrograms = new ArrayList<HashMap<String, Integer>>(2) {
        {
            for (int i = 0; i < 2; ++i) {
                HashMap<String, Integer> hashMap = new HashMap<>();
                hashMap.put(PROGRAM_ID, 0);
                hashMap.put(POSITION_COORDINATE, -1);
                hashMap.put(TEXTURE_UNIFORM, -1);
                hashMap.put(TEXTURE_COORDINATE, -1);
                add(hashMap);
            }
        }
    };

    public GLDrawer(Engage en, int surfaceW, int surfaceH)
    {
        this.width = en.getmPreviewWidth();
        this.height = en.getmPreviewHeight();
        this.mSurfaceWidth = surfaceW;
        this.mSurfaceHeight = surfaceH;
        this.engage = en;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(GLUtil.CUBE.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(GLUtil.CUBE);
        vertexBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(GLUtil.TEXTURE_NO_ROTATION.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(GLUtil.TEXTURE_NO_ROTATION);
        textureVerticesBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        mGLSaveTextureBuffer = ByteBuffer.allocateDirect(GLUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLSaveTextureBuffer.put(GLUtil.getRotation(0, false, true)).position(0);
        //rgba
        byteBuffer = ByteBuffer.allocateDirect(width * height * 4);

        initProgram(CAMERA_INPUT_FRAGMENT_SHADER_OES, mArrayPrograms.get(0));
        initProgram(CAMERA_INPUT_FRAGMENT_SHADER, mArrayPrograms.get(1));

        initFrameBuffers(width, height);

        mIsInitialized = true;
    }

    private void initProgram(String fragment, HashMap<String, Integer> programInfo) {
        int proID = programInfo.get(PROGRAM_ID);
        if (proID == 0) {
            proID = GLUtil.createProgram(CAMERA_INPUT_VERTEX_SHADER, fragment);
            programInfo.put(PROGRAM_ID, proID);
            programInfo.put(POSITION_COORDINATE, GLES20.glGetAttribLocation(proID, POSITION_COORDINATE));
            programInfo.put(TEXTURE_UNIFORM, GLES20.glGetUniformLocation(proID, TEXTURE_UNIFORM));
            programInfo.put(TEXTURE_COORDINATE, GLES20.glGetAttribLocation(proID, TEXTURE_COORDINATE));
        }
    }

    public int preProcess(int textureId) {
        if (mFrameBuffers == null
                || !mIsInitialized)
            return -2;

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, width, height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLUtil.checkGlError("glBindFramebuffer");

        GLES20.glUseProgram(mArrayPrograms.get(0).get(PROGRAM_ID));
        GLUtil.checkGlError("glUseProgram");

        vertexBuffer.position(0);
        int glAttribPosition = mArrayPrograms.get(0).get(POSITION_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        mTextureBuffer.position(0);
        int glAttribTextureCoordinate = mArrayPrograms.get(0).get(TEXTURE_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mArrayPrograms.get(0).get(TEXTURE_UNIFORM), 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);


        if (byteBuffer != null) {
            byteBuffer.rewind();
            GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
        }
        GLUtil.checkGlError("after read pixels");

        if (engage != null) {
            ((CameraEngage)engage).setByteBuffer(byteBuffer);
            ((CameraEngage)engage).signalHandleFrame();
        }
        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFrameBufferTextures[0];
    }

    public void draw(int textureId)
    {
        if (!mIsInitialized) {
            return;
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);

        GLES20.glUseProgram(mArrayPrograms.get(1).get(PROGRAM_ID));
        vertexBuffer.position(0);
        int glAttribPosition = mArrayPrograms.get(1).get(POSITION_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        textureVerticesBuffer.position(0);
        int glAttribTextureCoordinate = mArrayPrograms.get(1).get(TEXTURE_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureVerticesBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        //real draw
        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mArrayPrograms.get(1).get(TEXTURE_UNIFORM), 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // release
        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public int saveTextureToFrameBuffer(int textureOutId,  ByteBuffer buffer) {
        if(mFrameBuffers == null) {
            return GLUtil.NO_TEXTURE;
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, width, height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[1]);

        GLES20.glUseProgram(mArrayPrograms.get(1).get(PROGRAM_ID));

        if(!mIsInitialized) {
            return GLUtil.NOT_INIT;
        }

        vertexBuffer.position(0);
        int glAttribPosition = mArrayPrograms.get(1).get(POSITION_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        mGLSaveTextureBuffer.position(0);
        int glAttribTextureCoordinate = mArrayPrograms.get(1).get(TEXTURE_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLSaveTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        if(textureOutId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureOutId);
            GLES20.glUniform1i(mArrayPrograms.get(1).get(TEXTURE_UNIFORM), 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        if(buffer != null) {
            GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        }

        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFrameBufferTextures[1];
    }

    public void initFrameBuffers(int width, int height) {
        destroyFrameBuffers();

        if (mFrameBuffers == null) {
            mFrameBuffers = new int[2];
            mFrameBufferTextures = new int[2];

            GLES20.glGenFramebuffers(2, mFrameBuffers, 0);
            GLES20.glGenTextures(2, mFrameBufferTextures, 0);

            GLUtil.bindFrameBuffer(mFrameBufferTextures[0], mFrameBuffers[0], width, height);
            GLUtil.bindFrameBuffer(mFrameBufferTextures[1], mFrameBuffers[1], width, height);
        }
    }

    public final void destroy() {
        mIsInitialized = false;
        destroyFrameBuffers();
        GLES20.glDeleteProgram(mArrayPrograms.get(0).get(PROGRAM_ID));
        GLES20.glDeleteProgram(mArrayPrograms.get(1).get(PROGRAM_ID));
    }

    public void destroyFrameBuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(2, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(2, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    public void adjustViewPort(
            int viewW,
            int viewh) {
        setmScaleType(ScaleType.CENTER_CROP);
        calculateVertexBuffer(viewW, viewh, width, height);
        adjustTextureBuffer(engage.getOrientation(), engage.isFlipHorizontal(), viewW, viewh, width, height);
    }

    private void setmScaleType(ScaleType scaleType) {
        this.mScaleType = scaleType;
    }

    private void calculateVertexBuffer(int displayW, int displayH, int imageW, int imageH) {
        {

            float[] cube = null;
            if (mScaleType == ScaleType.CENTER_CROP || mScaleType == ScaleType.CENTER_STRETCH) {
                cube = GLUtil.CUBE;
            } else if (mScaleType == ScaleType.CENTER_INSIDE) {

                int outputHeight = displayH;
                int outputWidth = displayW;

                float ratio1 = (float) outputWidth / imageW;
                float ratio2 = (float) outputHeight / imageH;
                float ratioMax = Math.max(ratio1, ratio2);
                int imageWidthNew = Math.round(imageW * ratioMax);
                int imageHeightNew = Math.round(imageH * ratioMax);

                float ratioWidth = imageWidthNew / (float) outputWidth;
                float ratioHeight = imageHeightNew / (float) outputHeight;

                cube = new float[]{
                        GLUtil.CUBE[0] / ratioHeight, GLUtil.CUBE[1] / ratioWidth,
                        GLUtil.CUBE[2] / ratioHeight, GLUtil.CUBE[3] / ratioWidth,
                        GLUtil.CUBE[4] / ratioHeight, GLUtil.CUBE[5] / ratioWidth,
                        GLUtil.CUBE[6] / ratioHeight, GLUtil.CUBE[7] / ratioWidth,
                };
            }

            if (vertexBuffer == null) {
                vertexBuffer = ByteBuffer.allocateDirect(cube.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
            }
            vertexBuffer.clear();
            vertexBuffer.put(cube).position(0);
        }
    }

    private void adjustTextureBuffer(int orientation, boolean flipVertical, int displayW, int displayH, int imageW, int imageH) {
        float[] textureCords = GLUtil.getRotation(orientation, true, flipVertical);

        Log.i(TAG, "adjustTextureBuffer orientation : " + orientation + ", flipVertical : " + flipVertical);
        //填充图片转换buffer
        if (mTextureBuffer == null) {
            mTextureBuffer = ByteBuffer.allocateDirect(textureCords.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }
        mTextureBuffer.clear();
        mTextureBuffer.put(textureCords).position(0);


        float[] texture = null;
        float[] textureNoRotation = GLUtil.TEXTURE_NO_ROTATION;
        if (mScaleType == ScaleType.CENTER_INSIDE || mScaleType == ScaleType.CENTER_STRETCH) {
            texture = textureNoRotation;
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            //适配屏幕
            int outputHeight = displayH;
            int outputWidth = displayW;

            //旋转
            if (orientation == 270 || orientation == 90) {
                outputWidth = displayH;
                outputHeight = displayW;
                int tmp = imageW;
                imageW = imageH;
                imageH = tmp;
            }

            float ratio1 = (float) outputWidth / (float)imageW;
            float ratio2 = (float) outputHeight / (float) imageH;
            float ratioMax = Math.max(ratio1, ratio2);

            int imageWidthNew = Math.round((float)imageW * ratioMax);
            int imageHeightNew = Math.round((float)imageH * ratioMax);

            float ratioWidth = (float) imageWidthNew / (float) outputWidth;
            float ratioHeight = (float) imageHeightNew / (float) outputHeight;

            float distHorizontal = (1 - 1 / ratioHeight) / 2.0F;
            float distVertical = (1 - 1 / ratioWidth) / 2.0F;

            texture = new float[]{
                    addDistance(textureNoRotation[0], distHorizontal), addDistance(textureNoRotation[1], distVertical),
                    addDistance(textureNoRotation[2], distHorizontal), addDistance(textureNoRotation[3], distVertical),
                    addDistance(textureNoRotation[4], distHorizontal), addDistance(textureNoRotation[5], distVertical),
                    addDistance(textureNoRotation[6], distHorizontal), addDistance(textureNoRotation[7], distVertical),
            };

            textureVerticesBuffer.clear();
            textureVerticesBuffer.put(texture).position(0);
        }
    }

    public enum ScaleType { CENTER_INSIDE, CENTER_CROP, CENTER_STRETCH}

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

}
