/*
 * Copyright (c) 2018 anqi.huang@outlook.com.
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

package com.journeyOS.core.api.location;

import com.journeyOS.core.api.ICoreApi;
import com.journeyOS.core.database.city.City;

public interface ILocation extends ICoreApi {

    /**
     * 添加监听location变化
     *
     * @param listener
     */
    void addChangedListener(LocationChangedListener listener);

    /**
     * 移除监听location变化
     *
     * @param listener
     */
    void removeChangedListener(LocationChangedListener listener);

    /**
     * 开始定位
     */
    void startLocation();

    /**
     * 取消定位
     */
    void stopLocation();

    /**
     * 获取定位到城市ID
     *
     * @return 城市ID
     */
    String getLocatedCityId();

    /**
     * 获取定位到城市
     *
     * @return 城市
     */
    City getLocatedCity();

    interface LocationChangedListener {
        void onLocationChanged(City city);
    }
}
