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

package com.journeyOS.core;

import com.journeyOS.literouter.RouterMsssage;

public class Messages extends RouterMsssage {
    public static final int BASE = 1;
    public static final int MSG_ADD_ITEM = BASE << 0;
    public static final int MSG_ADD_GESTURE_APP = BASE << 1;
    public static final int MSG_ADD_GESTURE_PAY = BASE << 2;
    public static final int MSG_ADD_GESTURE_KEY = BASE << 3;
    public static final int MSG_ADD_GESTURE_MUSIC = BASE << 4;
    public static final int MSG_ADD_GESTURE_EDGE = BASE << 5;
    public static final int MSG_SCENE_REMOVE_GAME = BASE << 6;
    public static final int MSG_SCENE_REMOVE_VIDEO = BASE << 7;
    public static final int MSG_SCENE_SELECTOR_APP = BASE << 8;

    public int what;

    public int arg1;

    public String arg2;

    public Object obj;
}
