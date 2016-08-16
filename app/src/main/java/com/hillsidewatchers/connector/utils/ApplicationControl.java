/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.hillsidewatchers.connector.utils;

import com.vuforia.State;


//  Interface to be implemented by the activity which uses SampleApplicationSession
public interface ApplicationControl
{

    // 初始化trakcer
    boolean doInitTrackers();
    // 加载tracker数据
    boolean doLoadTrackersData();
    //启动追踪器tracker
    boolean doStartTrackers();
    // 停止追踪器tracker
    boolean doStopTrackers();
    // 销毁tracker数据
    boolean doUnloadTrackersData();
    // 释放tracker对象
    boolean doDeinitTrackers();

    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
    //vuforia初始化结束后
    void onInitARDone(ApplicationException e);

    // This callback is called every cycle
    void onVuforiaUpdate(State state);
    
}
