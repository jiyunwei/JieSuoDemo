package com.jyw.jiesuodemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static com.jyw.jiesuodemo.MyNineView.Pointer.WEIXUAN;
import static com.jyw.jiesuodemo.MyNineView.Pointer.XUANZHONG;

public class MyNineView extends View {

    private static final String TAG = "jyw";

    private Bitmap mBitmapWei;
    private Bitmap mBitmapXuan;
    private Bitmap mBitmapLine;
    private Pointer[][] pointers = new Pointer[3][3];
    private Pointer[][] pointCenters = new Pointer[3][3];
    Paint mPaint;
    //点的半径
    private int pointerRadius;
    private float pointBS = 6.0f;
    private Matrix mMatrix;
    private float lineRadius;
    private List<Pointer> selectedPointers;
    private Pointer mEventPointer;
    private int mIndex1, mIndex2;


    public MyNineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mMatrix = new Matrix();
        initBitmap();
        initPointer();

        selectedPointers = new ArrayList<>();


    }


    private void initPointer() {
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        int heightPixels = getResources().getDisplayMetrics().heightPixels;

        pointerRadius = mBitmapWei.getWidth() / 2;
        lineRadius = mBitmapLine.getHeight() / 2.0f;
        float centerPointerX = widthPixels / 2 - pointerRadius;
        float centerPointerY = heightPixels / 2 - pointerRadius;
        float distance = pointerRadius * pointBS;

        pointers[0][0] = new Pointer(centerPointerX - distance, centerPointerY - distance);
        pointers[0][1] = new Pointer(centerPointerX, centerPointerY - distance);
        pointers[0][2] = new Pointer(centerPointerX + distance, centerPointerY - distance);

        pointers[1][0] = new Pointer(centerPointerX - distance, centerPointerY);
        pointers[1][1] = new Pointer(centerPointerX, centerPointerY);
        pointers[1][2] = new Pointer(centerPointerX + distance, centerPointerY);

        pointers[2][0] = new Pointer(centerPointerX - distance, centerPointerY + distance);
        pointers[2][1] = new Pointer(centerPointerX, centerPointerY + distance);
        pointers[2][2] = new Pointer(centerPointerX + distance, centerPointerY + distance);


        //初始化中心点位置
        centerPointerX = widthPixels / 2;
        centerPointerY = heightPixels / 2;

        pointCenters[0][0] = new Pointer(centerPointerX - distance, centerPointerY - distance);
        pointCenters[0][1] = new Pointer(centerPointerX, centerPointerY - distance);
        pointCenters[0][2] = new Pointer(centerPointerX + distance, centerPointerY - distance);

        pointCenters[1][0] = new Pointer(centerPointerX - distance, centerPointerY);
        pointCenters[1][1] = new Pointer(centerPointerX, centerPointerY);
        pointCenters[1][2] = new Pointer(centerPointerX + distance, centerPointerY);

        pointCenters[2][0] = new Pointer(centerPointerX - distance, centerPointerY + distance);
        pointCenters[2][1] = new Pointer(centerPointerX, centerPointerY + distance);
        pointCenters[2][2] = new Pointer(centerPointerX + distance, centerPointerY + distance);


    }

    private void initBitmap() {
        mBitmapWei = BitmapFactory.decodeResource(getResources(), R.mipmap.weixuan);
        mBitmapXuan = BitmapFactory.decodeResource(getResources(), R.mipmap.xuanzhong);
        mBitmapLine = BitmapFactory.decodeResource(getResources(), R.mipmap.guang);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < pointers.length; i++) {
            for (int j = 0; j < pointers[i].length; j++) {
                Pointer pointer = pointers[i][j];
                if (pointer.selected_Status == XUANZHONG) {
                    canvas.drawBitmap(mBitmapXuan, pointer.x, pointer.y, mPaint);
                    Pointer pointer1 = pointCenters[i][j];


                } else {
                    canvas.drawBitmap(mBitmapWei, pointer.x, pointer.y, mPaint);
                }

            }
        }
//        canvas.drawBitmap(mBitmapWei, pointers[0][0].x - pointerRadius, pointers[0][0].y - pointerRadius, mPaint);

        if (selectedPointers != null && selectedPointers.size() > 0) {
           drawAllLine(canvas);
        }


    }

    /**
     * 画所有的线
     * @param canvas
     */
    private void drawAllLine(Canvas canvas) {

        Pointer pFrom = selectedPointers.get(0);
        for(int i=0;i<selectedPointers.size();i++){
            Pointer pTo = selectedPointers.get(i);
            drawLine(canvas,pFrom,pTo);

            pFrom = pTo;
        }

        drawLine(canvas, selectedPointers.get(selectedPointers.size() - 1), mEventPointer);
    }

    /**
     * @param pointer1
     */
    private void isInSelectedPointer(Pointer pointer1) {
    }

    /**
     * 划线
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas, Pointer myPointerFrom, Pointer myPointerTo) {

        float centerX = myPointerFrom.x;
        float centerY = myPointerFrom.y;
        mMatrix.postTranslate(centerX, centerY - lineRadius);
        mMatrix.postScale(caculationScale(myPointerFrom,myPointerTo), 1.0f, centerX, centerY);
        mMatrix.postRotate((float) caculatorDegress(myPointerFrom, myPointerTo), centerX, centerY);

        canvas.drawBitmap(mBitmapLine, mMatrix, mPaint);
        mMatrix.reset();
    }

    /**
     * 计算线长 线的缩放比例
     * @param myPointerFrom
     * @param myPointerTo
     * @return
     */
    private float caculationScale(Pointer myPointerFrom, Pointer myPointerTo) {
        double zDistance = caculationDistance(myPointerFrom, myPointerTo);
        double lineScale =zDistance/ mBitmapLine.getWidth();

        return (float) lineScale;
    }

    private double caculationDistance(Pointer myPointerFrom, Pointer myPointerTo) {
        float xFrom = myPointerFrom.x;
        float yFrom = myPointerFrom.y;
        float xTo = myPointerTo.x;
        float yTo = myPointerTo.y;

        float xDistance = Math.abs(xFrom - xTo);
        float yDistance = Math.abs(yFrom - yTo);
        float z2 = xDistance * xDistance + yDistance * yDistance;
        double zDistance = Math.sqrt(z2);
        return zDistance;

    }

    private double caculatorDegress(Pointer myPointerFrom, Pointer myPointerTo) {
        float xFrom = myPointerFrom.x;
        float yFrom = myPointerFrom.y;
        float xTo = myPointerTo.x;
        float yTo = myPointerTo.y;

        //无弧度区域的判断
        if (xFrom <= xTo && yFrom == yTo) {
            return 0;
        }

        //无弧度区域的判断
        if (xFrom > xTo && yFrom == yTo) {
            return 180;
        }
        //无弧度区域的判断
        if (xFrom == xTo && yFrom < yTo) {
            return 90;
        }
        //无弧度区域的判断
        if (xFrom == xTo && yFrom > yTo) {
            return 270;
        }

        //两个直角边的比
        double tanValue = Math.abs(yFrom - yTo) / Math.abs(xFrom - xTo);

        //atan() 将两个直角边的比作为参数求出弧度值  通过toDegress 求出锐角的角度值
        double atanValue = Math.toDegrees(Math.atan(tanValue));

        if (yTo > yFrom) {
            if (xFrom > xTo) {
                //2 向限
                return 180 - atanValue;
            } else {
                //1 向限
                return atanValue;
            }
        } else {
            if (xFrom > xTo) {
                //3 向限
                return 180 + atanValue;
            } else {
                //4 向限
                return 360 - atanValue;
            }
        }

//        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                mEventPointer = new Pointer(x, y);
                caculationDistance(x, y);

                break;
            case MotionEvent.ACTION_UP:
                reset();
                break;
        }

        invalidate();

        return true;
    }

    /**
     * 复位选中的点
     */
    private void reset() {
        for (Pointer pointer : selectedPointers) {
            pointer.selected_Status = WEIXUAN;
        }

        for(int i=0;i<pointers.length;i++){
            Pointer[] pointer = pointers[i];
            for(Pointer pointer1:pointer){
                pointer1.selected_Status = WEIXUAN;
            }
        }

        selectedPointers.clear();
    }

    /**
     * 计算当前点击的位置是否在某个点的范围内
     *
     * @param x
     * @param y
     */
    private void caculationDistance(float x, float y) {
        for (int i = 0; i < pointCenters.length; i++) {
            for (int j = 0; j < pointCenters[i].length; j++) {
                Pointer pointer = pointCenters[i][j];
                float distanceX = Math.abs(x - pointer.x);
                float distanceY = Math.abs(y - pointer.y);

                float sanbian2 = distanceX * distanceX + distanceY * distanceY;
                double sqrt = Math.sqrt(sanbian2);
                if (sqrt <= pointerRadius) {
                    Pointer clickPoint = pointers[i][j];

                    clickPoint.selected_Status = XUANZHONG;


                    if (!selectedPointers.contains(pointer)) {
                        selectedPointers.add(pointer);
                        mIndex1 = i;
                        mIndex2 = j;
                    }


                }
//                else{
//                    Pointer clickPoint = pointers[i][j];
//                    clickPoint.selected_Status = WEIXUAN;
//                }
            }
        }
    }

    class Pointer {
        public static final int WEIXUAN = 0;
        public static final int XUANZHONG = 1;

        public float x, y;
        public int selected_Status = WEIXUAN;

        public Pointer(float x, float y) {
            super();
            this.x = x;
            this.y = y;
        }


//        public boolean equals(Pointer obj) {
//            return this.x==obj.x && this.y == obj.y;
//        }


        public boolean equals(Pointer obj) {

            return this.x == obj.x && this.y == obj.y;

        }
    }


}
