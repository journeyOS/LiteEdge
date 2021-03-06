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

package com.journeyOS.core.database.music;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import com.journeyOS.core.database.DBConfigs;

@Deprecated
@Entity(tableName = DBConfigs.MUSIC_TABLE, primaryKeys = {DBConfigs.MUSIC_PACKAGE})
public class Music {

    @NonNull
    @ColumnInfo(name = DBConfigs.MUSIC_PACKAGE)
    public String packageName = "";

    /**
     * json
     * @ com.journeyOS.core.database.music.MusicConfig
     */
    @ColumnInfo(name = DBConfigs.MUSIC_CONFIG)
    public String config;

}
