/*
 * Copyright (c) 2018 anqi.huang@outlook.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.journeyOS.edge.view;

import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.journeyOS.base.Constant;
import com.journeyOS.base.persistence.SpUtils;
import com.journeyOS.core.CoreManager;
import com.journeyOS.core.api.edgeprovider.IBallProvider;
import com.journeyOS.core.api.thread.ICoreExecutors;
import com.journeyOS.core.database.ball.Ball;
import com.journeyOS.core.type.FingerDirection;
import com.journeyOS.edge.R;

public class OutterView extends FrameLayout implements InnerView.OnGestureListener, View.OnAttachStateChangeListener {

    private static final String TAG = OutterView.class.getSimpleName();

    private Context mContext;
    /**
     * 记录悬浮View的宽度
     */
    public static int viewWidth;

    /**
     * 记录悬浮View的高度
     */
    public static int viewHeight;

    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;

    /**
     * 用于更新悬浮View的位置
     */
    private WindowManager windowManager;

    /**
     * 悬浮View的布局参数
     */
    private WindowManager.LayoutParams mParams;

    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在悬浮View的上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在悬浮View的上的纵坐标的值
     */
    private float yInView;

    /**
     * 判定轻微滑动的阈值
     */
    private int mScaleTouchSlop;
    /**
     * 上一次Down事件触发的时间点
     */
    private long mLastDownTime;
    /**
     * 判定为长按的时间阈值
     */
    private final static long LONG_CLICK_LIMIT = 300;

    private float mMotionDownX;
    private float mMotionDownY;
    /**
     * 从一次down到up/cancel之间所有move事件中，触发点与down事件的触发点的最大偏移量（取横、纵坐标的较大值）
     */
    private float mMaxMoveDistance;

    /**
     * 震动服务
     */
    private Vibrator mVibrator;
    private long[] mPattern = {0, 80};

    private InnerView mInnerView;

    public OutterView(Context context) {
        this(context, null);
        this.mContext = context;
        addOnAttachStateChangeListener(this);
    }

    public OutterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
        addOnAttachStateChangeListener(this);
    }

    public OutterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.layout_ball, this);
        InnerView view = (InnerView) findViewById(R.id.floatView);
        mInnerView = view;
        view.setOnGestureListener(this);
        int ballSize = SpUtils.getInstant().getInt(Constant.BALL_SIZE, Constant.BALL_SIZE_DEFAULT);
        viewWidth = ballSize;
        viewHeight = ballSize;
        addOnAttachStateChangeListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //记录down事件触发的时间点
                mLastDownTime = System.currentTimeMillis();
                mMotionDownX = event.getX();
                mMotionDownY = event.getY();
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                getMaxMoveDistance(event);
                if (isLongTouch()) {
                    mVibrator.vibrate(mPattern, -1);
                    doLongPressEffect();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isClick(event)) {
                    doClickEffect();
                }
                resetMaxMoveDistance();
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                getMaxMoveDistance(event);
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新悬浮控件的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetMaxMoveDistance();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 将悬浮View的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新悬浮View在屏幕中的位置。
     */
    private void updateViewPosition() {
//		Log.i(TAG, "updateViewPosition(1) xInView:" + xInView + ",	yInView:" + yInView);
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);

        CoreManager.getDefault().getImpl(ICoreExecutors.class).diskIOThread().execute(new Runnable() {
            @Override
            public void run() {
                int orientation = mContext.getResources().getConfiguration().orientation;
                Ball config = new Ball();
                config.orientation = orientation;
                config.layoutX = mParams.x;
                config.layoutY = mParams.y;
                CoreManager.getDefault().getImpl(IBallProvider.class).insertOrUpdateConfig(config);
            }
        });
        int ballSize = SpUtils.getInstant().getInt(Constant.BALL_SIZE, Constant.BALL_SIZE_DEFAULT);
        mParams.width = ballSize;
        mParams.height = ballSize;
        windowManager.updateViewLayout(this, mParams);
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
//        if (statusBarHeight == 0) {
//            try {
//                Class<?> c = Class.forName("com.android.internal.R$dimen");
//                Object o = c.newInstance();
//                Field field = c.getField("status_bar_height");
//                int x = (Integer) field.get(o);
//                statusBarHeight = getResources().getDimensionPixelSize(x);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return statusBarHeight;

        //添加LayoutParams.FLAG_LAYOUT_IN_SCREEN之后，不需要计算状态栏高度
        return 0;
    }

    /**
     * 是否是长按
     *
     * @return
     */
    private boolean isLongTouch() {
        long time = System.currentTimeMillis();
        //需要满足两个条件：1.所有move事件与down事件之间的偏移量小于阈值，2.该move事件与down之间之间的时间差大于阈值
        if (mMaxMoveDistance < mScaleTouchSlop && (time - mLastDownTime >= LONG_CLICK_LIMIT)) {
            return true;
        }
        return false;
    }

    /**
     * 是否是点击
     *
     * @param event
     * @return
     */
    private boolean isClick(MotionEvent event) {
        //满足一个条件：1.所有move事件与down事件之间的偏移量小于阈值
        if (mMaxMoveDistance < mScaleTouchSlop) {
            return true;
        }
        return false;
    }

    /**
     * 所有move事件与down事件的坐标偏移量最大值
     *
     * @param event
     */
    private void getMaxMoveDistance(MotionEvent event) {
        float dx = Math.abs(event.getX() - mMotionDownX);
        float dy = Math.abs(event.getY() - mMotionDownY);
        float maxValue = Math.max(dx, dy);
        mMaxMoveDistance = Math.max(mMaxMoveDistance, maxValue);
    }

    private void resetMaxMoveDistance() {
        mMaxMoveDistance = 0;
    }

    /**
     * 执行点击效果
     */
    private void doClickEffect() {
        Log.d(TAG, "doClickEffect() called");
        final InnerView child = (InnerView) findViewById(R.id.floatView);
        child.startClickAnimator();
    }

    /**
     * 执行长按效果
     */
    private void doLongPressEffect() {
        Log.d(TAG, "doLongPressEffect() called");
//		final InnerView child = (InnerView) findViewById(R.id.floatView);
//				child.startLongPressAnim();
        if (gestureListener != null)
            gestureListener.onGesture(FingerDirection.LONG_PRESS);
    }

    public InnerView getInnerBall() {
        return mInnerView;
    }

    @Override
    public void onGesture(FingerDirection fingerDirection) {
        if (gestureListener != null) {
            gestureListener.onGesture(fingerDirection);
        }
    }


    private OnGestureListener gestureListener;

    public void setOnGestureListener(OnGestureListener inf) {
        gestureListener = inf;
    }

    @Override
    public void onViewAttachedToWindow(View view) {
        if (gestureListener != null) {
            gestureListener.onViewAttachedToWindow();
        }
    }

    @Override
    public void onViewDetachedFromWindow(View view) {
        if (gestureListener != null) {
            gestureListener.onViewDetachedFromWindow();
        }
    }

    public interface OnGestureListener {
        void onGesture(FingerDirection fingerDirection);

        void onViewAttachedToWindow();

        void onViewDetachedFromWindow();
    }
}