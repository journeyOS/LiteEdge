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

package com.journeyOS.core.api.thread;


import com.journeyOS.core.api.ICoreApi;

import java.util.concurrent.Executor;

public interface ICoreExecutors extends ICoreApi {

    /**
     * 文件操作线程
     *
     * @return 线程
     */
    Executor diskIOThread();

    /**
     * 网络线程
     *
     * @return 线程
     */
    Executor networkIOThread();

    /**
     * 主线程
     *
     * @return 线程
     */
    Executor mainThread();

}

