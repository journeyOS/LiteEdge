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

package com.journeyOS.core.push;

import com.journeyOS.core.database.user.EdgeUser;

import cn.bmob.v3.BmobInstallation;

public class Installation extends BmobInstallation {

    public EdgeUser author;

    /**
     * 手机厂商
     */
    public String brand;

    /**
     * 手机型号
     */
    public String model;

    /**
     * 手机系统版本号
     */
    public String version;

    /**
     * 当前软件版本号
     */
    public String appVersion;
}
