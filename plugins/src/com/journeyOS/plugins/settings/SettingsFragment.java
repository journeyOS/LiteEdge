/*
 * Copyright (c) 2019 anqi.huang@outlook.com
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

package com.journeyOS.plugins.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.Fragment;

import com.journeyOS.base.Constant;
import com.journeyOS.base.persistence.SpUtils;
import com.journeyOS.base.widget.SettingSwitch;
import com.journeyOS.core.CoreManager;
import com.journeyOS.core.api.edge.IEdge;
import com.journeyOS.core.api.plugins.IPlugins;
import com.journeyOS.core.api.ui.IContainer;
import com.journeyOS.core.base.BaseFragment;
import com.journeyOS.core.permission.IPermission;
import com.journeyOS.plugins.R;
import com.journeyOS.plugins.R2;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingsFragment extends BaseFragment {

    @BindView(R2.id.daemon)
    SettingSwitch mDaemon;

    @BindView(R2.id.exclude)
    SettingSwitch mExclude;

    @BindView(R2.id.ball)
    SettingSwitch mBall;

    static Activity mContext;

    public static Fragment newInstance(Activity activity) {
        SettingsFragment fragment = new SettingsFragment();
        mContext = activity;
        return fragment;
    }

    @Override
    public int attachLayoutRes() {
        return R.layout.fragment_settings;
    }

    @Override
    public void initBeforeView() {
        super.initBeforeView();
    }

    @Override
    public void initViews() {
        boolean daemon = SpUtils.getInstant().getBoolean(Constant.DAEMON, Constant.DAEMON_DEFAULT);
        mDaemon.setCheck(daemon);

        boolean exclude = SpUtils.getInstant().getBoolean(Constant.EXCLUDE, Constant.EXCLUDE_DEFAULT);
        mExclude.setCheck(exclude);

        boolean ball = SpUtils.getInstant().getBoolean(Constant.BALL, Constant.BALL_DEFAULT);
        mBall.setCheck(ball);
        if (ball && CoreManager.getDefault().getImpl(IPermission.class).canDrawOverlays(mContext)) {
            CoreManager.getDefault().getImpl(IEdge.class).showingOrHidingBall(true);
        }
    }

    @OnClick({R2.id.daemon})
    public void listenerAutoStart() {
        boolean daemon = SpUtils.getInstant().getBoolean(Constant.DAEMON, Constant.DAEMON_DEFAULT);
        mDaemon.setCheck(!daemon);
        SpUtils.getInstant().put(Constant.DAEMON, !daemon);
    }

    @OnClick({R2.id.exclude})
    public void listenerExclude() {
        boolean exclude = SpUtils.getInstant().getBoolean(Constant.EXCLUDE, Constant.EXCLUDE_DEFAULT);
        mExclude.setCheck(!exclude);
        SpUtils.getInstant().put(Constant.EXCLUDE, !exclude);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.AppTask> tasks = am.getAppTasks();
                if (tasks != null && tasks.size() > 0) {
                    ActivityManager.AppTask task = tasks.get(0);
                    if (task != null) {
                        task.setExcludeFromRecents(!exclude);
                    }
                }
            }
        }
    }

    @OnClick({R2.id.ball})
    public void listenerBall() {
        if (!CoreManager.getDefault().getImpl(IPermission.class).canDrawOverlays(mContext)) {
            CoreManager.getDefault().getImpl(IPermission.class).drawOverlays(mContext, true);
            return;
        }

        boolean ball = SpUtils.getInstant().getBoolean(Constant.BALL, Constant.BALL_DEFAULT);
        mBall.setCheck(!ball);
        SpUtils.getInstant().put(Constant.BALL, !ball);
        CoreManager.getDefault().getImpl(IEdge.class).showingOrHidingBall(!ball);
    }

    @OnClick({R2.id.portrait})
    public void listenerPortrait() {
        Fragment fragment = CoreManager.getDefault().getImpl(IPlugins.class).provideGestureFragment(mContext, Configuration.ORIENTATION_PORTRAIT);
        CoreManager.getDefault().getImpl(IContainer.class).subActivity(mContext, fragment, mContext.getString(R.string.gesture_portrait));
    }

    @OnClick({R2.id.landscape})
    public void listenerLandscape() {
        Fragment fragment = CoreManager.getDefault().getImpl(IPlugins.class).provideGestureFragment(mContext, Configuration.ORIENTATION_LANDSCAPE);
        CoreManager.getDefault().getImpl(IContainer.class).subActivity(mContext, fragment, mContext.getString(R.string.gesture_landscape));
    }
}
