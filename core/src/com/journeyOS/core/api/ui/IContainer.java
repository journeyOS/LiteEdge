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

package com.journeyOS.core.api.ui;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.journeyOS.core.api.ICoreApi;

public interface IContainer extends ICoreApi {
    void subActivity(Context context, Fragment fragment, String title);
    void subWithMenuActivity(Context context, Fragment fragment, String title);
}
