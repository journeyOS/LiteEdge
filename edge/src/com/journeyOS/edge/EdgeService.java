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

package com.journeyOS.edge;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.android.fbi.ad.EmptyActivity;
import com.journeyOS.base.Constant;
import com.journeyOS.base.persistence.SpUtils;
import com.journeyOS.base.receiver.GlobalActionObserver;
import com.journeyOS.base.receiver.ScreenObserver;
import com.journeyOS.base.utils.LogUtils;
import com.journeyOS.core.CoreManager;
import com.journeyOS.core.OnlineManager;
import com.journeyOS.core.StateMachine;
import com.journeyOS.core.api.edge.IEdge;
import com.journeyOS.core.api.edgeprovider.IAppProvider;
import com.journeyOS.core.api.edgeprovider.IEdgeLabProvider;
import com.journeyOS.core.api.edgeprovider.IEdgeProvider;
import com.journeyOS.core.api.edgeprovider.IGestureProvider;
import com.journeyOS.core.api.thread.ICoreExecutors;
import com.journeyOS.core.database.online.ConfigsAir;
import com.journeyOS.core.type.BallState;
import com.journeyOS.core.type.EdgeDirection;
import com.journeyOS.core.type.FingerDirection;
import com.journeyOS.edge.utils.NotificationUtils;
import com.journeyOS.edge.wm.BallManager;
import com.journeyOS.i007Service.DataResource;
import com.journeyOS.i007Service.DataResource.FACTORY;
import com.journeyOS.i007Service.I007Manager;
import com.journeyOS.i007Service.interfaces.II007Listener;

public class EdgeService extends Service implements GlobalActionObserver.GlobalActionListener {
    private static final String TAG = EdgeService.class.getSimpleName();

    private Context mContext;

    private II007Listener mII007Listener = new II007Listener.Stub() {
        @Override
        public void onSceneChanged(long l, String s, String s1) throws RemoteException {
            handleSceneChanged(l, s, s1);
        }
    };

    final IEdgeInterface.Stub mBinder = new IEdgeInterface.Stub() {
        @Override
        public void showingBall(final boolean isShowing) throws RemoteException {
            LogUtils.d(TAG, "showing ball = " + isShowing);
            CoreManager.getDefault().getImpl(ICoreExecutors.class).mainThread().execute(new Runnable() {
                @Override
                public void run() {
                    if (isShowing) {
                        BallManager.getDefault().showing();
                        BallManager.getDefault().setOnBallViewListener(new BallManager.OnBallViewListener() {
                            @Override
                            public void onGesture(FingerDirection fingerDirection) {
                                LogUtils.d(TAG, "on ball fingerDirection = " + fingerDirection.name());
                                Dispatcher.getDefault().handleGestureDirection(fingerDirection);
                            }
                        });
                    } else {
                        BallManager.getDefault().hiding();
                    }
                }
            });
        }

        @Override
        public void showingEdge(int direction) throws RemoteException {
            CoreManager.getDefault().getImpl(IEdge.class).showingEdge(direction);
        }

        @Override
        public void showingEdgeDelayed(int direction, long delayMillis) throws RemoteException {
            CoreManager.getDefault().getImpl(IEdge.class).showingEdge(direction, delayMillis);
        }

        @Override
        public void hidingEdge(boolean isAnimator) throws RemoteException {
            CoreManager.getDefault().getImpl(IEdge.class).hidingEdge(isAnimator);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = CoreManager.getDefault().getContext();
        CoreManager.getDefault().setRunning(true);
        LogUtils.d(TAG, "edge service create!");
        prepraJob();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e(TAG, "edge service destroy!");
        BallManager.getDefault().hiding();
        ScreenObserver.getDefault().stopScreenStateUpdate(mContext);
        CoreManager.getDefault().setRunning(false);
        I007Manager.unregisterListener(mII007Listener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        BallManager.getDefault().updateViewLayout(SpUtils.getInstant().getInt(Constant.BALL_SIZE, Constant.BALL_SIZE_DEFAULT));
        CoreManager.getDefault().getImpl(IEdge.class).hidingEdge(true);
    }

    @Override
    public void onPressHomeKey() {
        if (EdgeDirection.NONE != StateMachine.getEdgeDirection()) {
            CoreManager.getDefault().getImpl(IEdge.class).hidingEdge(true);
        }
    }

    @Override
    public void onPressRecentapps() {
        if (EdgeDirection.NONE != StateMachine.getEdgeDirection()) {
            CoreManager.getDefault().getImpl(IEdge.class).hidingEdge(true);
        }
    }

    void prepraJob() {
        this.startForeground(NotificationUtils.NOTIFICATION_ID, NotificationUtils.getNotification(mContext));
        this.stopForeground(true);

        GlobalActionObserver.getDefault().startGlobalActionReceiver(mContext);
        GlobalActionObserver.getDefault().setOnGlobalActionListener(this);

        final long factors = I007Manager.SCENE_FACTOR_LCD | I007Manager.SCENE_FACTOR_APP;
        I007Manager.registerListener(factors, mII007Listener);

        CoreManager.getDefault().getImpl(ICoreExecutors.class).diskIOThread().execute(new Runnable() {
            @Override
            public void run() {
                CoreManager.getDefault().getImpl(IEdgeProvider.class).initConfig();
                CoreManager.getDefault().getImpl(IEdgeLabProvider.class).initConfig();
                CoreManager.getDefault().getImpl(IGestureProvider.class).initConfig();
                CoreManager.getDefault().getImpl(IAppProvider.class).checkApps();
            }
        });

        boolean barrage = SpUtils.getInstant().getBoolean(Constant.BARRAGE, Constant.BARRAGE_DEFAULT);
//        if (barrage) {
        NotificationManager.getDefault().startNotificationService();
//        }
        OnlineManager.getDefault().syncConfigs();
    }

    void handleSceneChanged(long factorId, String status, String packageName) {
        FACTORY factory = I007Manager.getFactory(factorId);
        LogUtils.d(TAG, "handle scene changed factorId = [" + factorId + "], status = [" + status + "], packageName = [" + packageName + "]");
        switch (factory) {
            case APP:
                CoreManager.getDefault().getImpl(IEdge.class).hidingEdge(true);
                handleScene(packageName);
                break;
            case LCD:
                boolean isScreenOn = I007Manager.isScreenOn(status);
                handleScreen(isScreenOn);
                break;
        }
    }

    void handleScreen(boolean isScreenOn) {
        if (Constant.DEBUG) {
            LogUtils.d(TAG, "edge service listener screen changed = " + isScreenOn);
        }

        if (isScreenOn) {
//            boolean barrage = SpUtils.getInstant().getBoolean(Constant.BARRAGE, Constant.BARRAGE_DEFAULT);
//            if (barrage) {
            NotificationManager.getDefault().startNotificationService();
//            }
        } else {
            CoreManager.getDefault().getImpl(IEdge.class).hidingEdge(false);
            I007Manager.keepAlive();
        }
    }

    void handleScene(String packageName) {
        //app类型
        DataResource.APP appType = I007Manager.getAppType(packageName);
        LogUtils.d(TAG, "handle scene changed, packageName = [" + packageName + "], scene = [" + appType + "]");
        switch (appType) {
            case GAME:
                handleAutoHideBall(Constant.AUTO_HIDE_BALL_GAME);
                break;
            case VIDEO:
                handleAutoHideBall(Constant.AUTO_HIDE_BALL_VIDEO);
                dispatchFBI(appType);
                break;
            case BROWSER:
                dispatchFBI(appType);
                break;
            case NEWS:
                dispatchFBI(appType);
                break;
            case READER:
                dispatchFBI(appType);
                break;
            case MUSIC:
                dispatchFBI(appType);
                break;
            case IM:
                break;
            case ALBUM:
                break;
            case DEFAULT:
                handleAutoHideBall(Constant.AUTO_HIDE_BALL_NONE);
                break;
        }
    }

    void handleAutoHideBall(int type) {
        //悬浮球总开关
        boolean ball = SpUtils.getInstant().getBoolean(Constant.BALL, Constant.BALL_DEFAULT);
        LogUtils.d(TAG, "handle auto hide ball, ball toogle = [" + ball + "]");
        if (ball) {
            BallState ballState = StateMachine.getBallState();
            LogUtils.d(TAG, "handle auto hide ball, ball state = [" + ballState + "]");
            if (type != 0) {
                //当前悬浮球状态
                if (BallState.HIDE == ballState) {
                    return;
                }
                int flags = SpUtils.getInstant().getInt(Constant.AUTO_HIDE_BALL, Constant.AUTO_HIDE_BALL_DEFAULT);
                LogUtils.d(TAG, "handle auto hide ball, scene = [" + flags + "]");
                //游戏中自动关闭悬浮球
                if ((flags & type) != 0) {
                    LogUtils.d(TAG, "hide ball in game");
                    CoreManager.getDefault().getImpl(IEdge.class).showingOrHidingBall(false);
                }
                //视频中自动关闭悬浮球
                if ((flags & type) != 0) {
                    LogUtils.d(TAG, "hide ball in video");
                    CoreManager.getDefault().getImpl(IEdge.class).showingOrHidingBall(false);
                }
            } else {
                if (BallState.HIDE == ballState) {
                    LogUtils.d(TAG, "show ball in normal");
                    CoreManager.getDefault().getImpl(IEdge.class).showingOrHidingBall(true);
                }
            }
        }
    }

    void dispatchFBI(DataResource.APP appType) {
        ConfigsAir configsAir = OnlineManager.getDefault().getOnlineConfigs();
        if (configsAir != null) {
            LogUtils.d(TAG, "air configs = [" + configsAir.adType + "]");
            if (configsAir.adType != null && configsAir.adType.contains(appType.name().toLowerCase())) {
                EmptyActivity.navigationActivity(mContext);
            }
        } else {
            EmptyActivity.navigationActivity(mContext);
        }
    }
}
