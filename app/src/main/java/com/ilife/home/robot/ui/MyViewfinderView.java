package com.ilife.home.robot.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.google.zxing.ResultPoint;
import com.ilife.home.robot.utils.DisplayUtil;
import com.ilife.home.robot.R;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengjiaping on 2017/7/5.
 */

public class MyViewfinderView extends ViewfinderView {
    private Context mContext;
    public int laserLinePosition = 0;
    public float[] position = new float[]{0f, 0.5f, 1f};
    public int[] colors = new int[]{0x00ffffff, 0xffffffff, 0x00ffffff};
    public LinearGradient linearGradient;

    public MyViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }


    /**
     * 重写draw方法绘制自己的扫描框
     *
     * @param canvas
     */
    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }

        Rect frame = framingRect;
        Rect previewFrame = previewFramingRect;

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        //绘制4个角

        paint.setColor(0xFFFFFFFF);//定义画笔的颜色
        canvas.drawRect(frame.left, frame.top, frame.left + 70, frame.top + 10, paint);
        canvas.drawRect(frame.left, frame.top, frame.left + 10, frame.top + 70, paint);

        canvas.drawRect(frame.right - 70, frame.top, frame.right, frame.top + 10, paint);
        canvas.drawRect(frame.right - 10, frame.top, frame.right, frame.top + 70, paint);

        canvas.drawRect(frame.left, frame.bottom - 10, frame.left + 70, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - 70, frame.left + 10, frame.bottom, paint);

        canvas.drawRect(frame.right - 70, frame.bottom - 10, frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - 10, frame.bottom - 70, frame.right, frame.bottom, paint);

        //================================================================================
        Rect targetRect = new Rect(frame.left, frame.bottom + 10, frame.right, frame.bottom + 60);
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(3);
        paint.setTextSize(DisplayUtil.sp2px(mContext, 18));
//        String testString = "测试：ijkJQKA:1234";
        String testString = mContext.getString(R.string.capture_aty_scan);
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(targetRect, paint);
        paint.setColor(Color.parseColor("#FFC125"));
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        // 转载请注明出处：http://blog.csdn.net/hursing
        int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(testString, targetRect.centerX(), baseline, paint);
        //================================================================================

//        canvas.drawText("扫描二维码",frame.left,frame.bottom,paint_text);
        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            //  paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            //  scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            int middle = frame.height() / 2 + frame.top;
            laserLinePosition = laserLinePosition + 5;
            if (laserLinePosition > frame.height()) {
                laserLinePosition = 0;
            }
            linearGradient = new LinearGradient(frame.left + 1, frame.top + laserLinePosition, frame.right - 1, frame.top + 10 + laserLinePosition, colors, position, Shader.TileMode.CLAMP);
            // Draw a red "laser scanner" line through the middle to show decoding is active

            //  paint.setColor(laserColor);
            paint.setShader(linearGradient);
            //绘制扫描线
            canvas.drawRect(frame.left + 1, frame.top + laserLinePosition, frame.right - 1, frame.top + 10 + laserLinePosition, paint);
            paint.setShader(null);
            float scaleX = frame.width() / (float) previewFrame.width();
            float scaleY = frame.height() / (float) previewFrame.height();

            List<ResultPoint> currentPossible = possibleResultPoints;
            List<ResultPoint> currentLast = lastPossibleResultPoints;
            int frameLeft = frame.left;
            int frameTop = frame.top;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new ArrayList<>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint);
                }
            }
            postInvalidateDelayed(16,
                    frame.left,
                    frame.top,
                    frame.right,
                    frame.bottom);
            // postInvalidate();
        }
    }
//    public MyViewfinderView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    @SuppressLint("DrawAllocation")
//    @Override
//    public void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        refreshSizes();
//        if (framingRect == null || previewFramingRect == null) {
//            return;
//        }
//
//        Rect frame = framingRect;
//        Rect previewFrame = previewFramingRect;
//
//        int width = canvas.getWidth();
//        int height = canvas.getHeight();
//        //下面是画四个角
//        paint.setColor(Color.YELLOW);//定义画笔的颜色
//        canvas.drawRect(frame.left, frame.top, frame.left+70, frame.top+10, paint);
//        canvas.drawRect(frame.left, frame.top, frame.left + 10, frame.top + 70, paint);
//
//        canvas.drawRect(frame.right-70, frame.top, frame.right, frame.top+10, paint);
//        canvas.drawRect(frame.right-10, frame.top, frame.right, frame.top+70, paint);
//
//        canvas.drawRect(frame.left, frame.bottom-10, frame.left+70, frame.bottom, paint);
//        canvas.drawRect(frame.left, frame.bottom-70, frame.left+10, frame.bottom, paint);
//
//        canvas.drawRect(frame.right-70, frame.bottom-10, frame.right, frame.bottom, paint);
//        canvas.drawRect(frame.right-10, frame.bottom-70, frame.right, frame.bottom, paint);
//
//        // Draw the exterior (i.e. outside the framing rect) darkened
//        paint.setColor(resultBitmap != null ? resultColor : maskColor);
//        canvas.drawRect(0, 0, width, frame.top, paint);
//        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
//        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
//        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
//
//        if (resultBitmap != null) {
//            // Draw the opaque result bitmap over the scanning rectangle
//            paint.setAlpha(CURRENT_POINT_OPACITY);
//            canvas.drawBitmap(resultBitmap, null, frame, paint);
//        } else {
//
//            // Draw a red "laser scanner" line through the middle to show decoding is active
//            paint.setColor(laserColor);
//            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//            int middle = frame.height() / 2 + frame.top;
//            canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
//
//            float scaleX = frame.width() / (float) previewFrame.width();
//            float scaleY = frame.height() / (float) previewFrame.height();
//
//            List<ResultPoint> currentPossible = possibleResultPoints;
//            List<ResultPoint> currentLast = lastPossibleResultPoints;
//            int frameLeft = frame.left;
//            int frameTop = frame.top;
//            if (currentPossible.isEmpty()) {
//                lastPossibleResultPoints = null;
//            } else {
//                possibleResultPoints = new ArrayList<>(5);
//                lastPossibleResultPoints = currentPossible;
//                paint.setAlpha(CURRENT_POINT_OPACITY);
//                paint.setColor(resultPointColor);
//                for (ResultPoint point : currentPossible) {
//                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                            frameTop + (int) (point.getY() * scaleY),
//                            POINT_SIZE, paint);
//                }
//            }
//            if (currentLast != null) {
//                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
//                paint.setColor(resultPointColor);
//                float radius = POINT_SIZE / 2.0f;
//                for (ResultPoint point : currentLast) {
//                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
//                            frameTop + (int) (point.getY() * scaleY),
//                            radius, paint);
//                }
//            }
//
//            // Request another update at the animation interval, but only repaint the laser line,
//            // not the entire viewfinder mask.
//            postInvalidateDelayed(ANIMATION_DELAY,
//                    frame.left - POINT_SIZE,
//                    frame.top - POINT_SIZE,
//                    frame.right + POINT_SIZE,
//                    frame.bottom + POINT_SIZE);
//        }
//    }
}
