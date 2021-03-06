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

package com.journeyOS.core.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

public class DBHelper {

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE city (id INTEGER NOT NULL, cityId TEXT, country TEXT, countryEn TEXT, cityName TEXT, province TEXT, provinceEn TEXT, longitude TEXT, latitude TEXT, PRIMARY KEY(id))");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE weather (cityId TEXT NOT NULL, weather TEXT, air TEXT, time TEXT, PRIMARY KEY(cityId))");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE user (userId TEXT NOT NULL, userName TEXT, icon TEXT, phone TEXT, email TEXT, token TEXT, PRIMARY KEY(userId))");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE app (packageName TEXT NOT NULL, appName TEXT, barrage INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(packageName))");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE edgeLab (edge TEXT NOT NULL, gravity INTEGER NOT NULL DEFAULT 1, orientation INTEGER NOT NULL DEFAULT 1, radius INTEGER NOT NULL DEFAULT 1, peek INTEGER NOT NULL DEFAULT 1, rotate INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(edge))");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE gesture (gestureDirection TEXT NOT NULL, orientation INTEGER NOT NULL DEFAULT 0, type TEXT, gestureAction TEXT, comment TEXT,PRIMARY KEY(gestureDirection))");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE edgeLab ADD COLUMN width INTEGER NOT NULL DEFAULT -1");
            database.execSQL("ALTER TABLE edgeLab ADD COLUMN height INTEGER NOT NULL DEFAULT -1");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE music (packageName TEXT NOT NULL, config TEXT, PRIMARY KEY(packageName))");
        }
    };


    public static <T extends RoomDatabase> T provider(Context context, Class<T> dbCls, String dbName) {
        return Room.databaseBuilder(context, dbCls, dbName)
                .addMigrations(MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9)
                .fallbackToDestructiveMigration().build();
    }
}
