package com.sheen.jgkit.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Sheen (Shen) Tian on 7/25/2016.
 */
public class VividImageView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    /**
     * Sensor manager.
     */
    private SensorManager mSensorManager;

    /**
     * Accelerometer.
     */
    private Sensor mAccelerometer = null;

    /**
     * Magnetic filed sensor.
     */
    private Sensor mMagneticFiled = null;

    /**
     * Accelerometer values.
     */
    private float accValues[] = new float[3];

    /**
     * Magnetic field values.
     */
    private float magValues[] = new float[3];

    /**
     * Rotation matrix.
     */
    private float r[] = new float[9];

    /**
     * X Y Z angle values.
     */
    private float values[] = new float[3];

    /**
     * X last offset.
     */
    private int xLastOffset = 0;

    /**
     * Y last offset
     */
    private int yLastOffset = 0;

    /**
     * Original drawable resource of the background.
     */
    private Drawable mBGDrawable;

    /**
     * Bitmap generated from the drawable background resource.
     */
    private Bitmap mBGBitmap;

    /**
     * Maximum offset of the background.
     */
    private final int mMaxViewOffset = 90;

    /**
     * Render thread.
     */
    private HandlerThread mRenderThread;

    /**
     * Constructor.
     * @param context
     * @param attrs
     */
    public VividImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Query sensor manager
        mSensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticFiled = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Set surface holder callback
        getHolder().addCallback(this);
    }

    /**
     * Sets the background image resource.
     * @param d
     */
    @Override
    public void setBackground(Drawable d) {
        if (null != d) {
            mBGDrawable = d;
        }
    }

    /**
     * Gets the background image resource.
     * @return
     */
    @Override
    public Drawable getBackground() {
        return mBGDrawable;
    }

    /**
     * Gets called when the surface is being created.
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Create and start the render thread
        mRenderThread = new HandlerThread("VividImageView.RenderThread");
        mRenderThread.start();

        Looper looper = mRenderThread.getLooper();
        Handler handler = new Handler(looper);

        // Register sensor listeners
        mSensorManager.registerListener(VividImageView.this,
                mAccelerometer, SensorManager.SENSOR_DELAY_GAME, handler);
        mSensorManager.registerListener(VividImageView.this,
                mMagneticFiled, SensorManager.SENSOR_DELAY_GAME, handler);
    }

    /**
     * Gets called when the surface changed.
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Generate the background bitmap
        GenerateBGBitmap(width, height);

        // Get the canvas and draw the bitmap
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(mBGBitmap, 0 - mMaxViewOffset, 0 - mMaxViewOffset, null);

        // Release the canvas
        holder.unlockCanvasAndPost(canvas);
    }

    /**
     * Gets called when the surface is destroyed..
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Unregister the sensor listener
        mSensorManager.unregisterListener(VividImageView.this);

        // Stop the thread and exit
        if (null != mRenderThread) {
            mRenderThread.quit();
            try {
                mRenderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates the background bitmap.
     * @param width
     * @param height
     */
    protected void GenerateBGBitmap( int width, int height) {
        // Create a new bitmap
        mBGBitmap = Bitmap.createBitmap(
                width + mMaxViewOffset * 2, height + mMaxViewOffset * 2, Bitmap.Config.ARGB_8888);

        // Create a canvas with the bitmap
        Canvas bgCanvas = new Canvas(mBGBitmap);
        bgCanvas.drawColor(Color.WHITE);

        // Draw the background image on the canvas
        if (mBGDrawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable)mBGDrawable;
            bitmapDrawable.setGravity(Gravity.CENTER);
            bitmapDrawable.setBounds(0, 0, mBGBitmap.getWidth(), mBGBitmap.getHeight());
            bitmapDrawable.draw(bgCanvas);
        }
    }

    /**
     * Gets called on sensor data changes.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get the sensor type
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magValues = event.values.clone();
                break;
        }

        // Get rotation matrix
        SensorManager.getRotationMatrix(r, null, accValues, magValues);

        // Compute the orientation
        SensorManager.getOrientation(r, values);

        // Get X angle
        int xAngle = (int) Math.toDegrees(values[2]);

        // Get Y angle
        int yAngle = (int) Math.toDegrees(values[1]);

        // Get Z angle, no need here
        //int zAngle = (int) Math.toDegrees(values[0]);

        Log.i("VividImageView", " X:" + xAngle + " Y:" + yAngle /*+ " Z:" + zAngle*/);

        /**
         *  Need a better filter algorithm to make the updating more smoothly.
         */
        int xOffset = 0;
        if (xAngle >= -180 && xAngle < -90) xOffset = 0 - (180 + xAngle);
        else if (xAngle >= -90 && xAngle <= 90) xOffset = xAngle;
        else if (xAngle > 90 && xAngle <= 180) xOffset = 180 - xAngle;

        int yOffset = 0;
        if (yAngle >= -180 && yAngle < -90) yOffset = 0 - (180 + yAngle);
        else if (yAngle >= -90 && yAngle <= 90) yOffset = yAngle;
        else if (yAngle > 90 && yAngle <= 180) yOffset = 180 - yAngle;

        Log.i("VividImageView", " X:" + xOffset + " Y:" + yOffset);

        if (xOffset != xLastOffset || yOffset != yLastOffset) {
            // Save the new offset values
            xLastOffset = xOffset;
            yLastOffset = yOffset;

            // Try to update the surface
            Canvas canvas = getHolder().lockCanvas();
            if (null != canvas) {
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(mBGBitmap, (0 - mMaxViewOffset) + xOffset, (0 - mMaxViewOffset) - yOffset, null);
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * Gets called when the accuracy of the sensor changes
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged (Sensor sensor,int accuracy){

    }
}

