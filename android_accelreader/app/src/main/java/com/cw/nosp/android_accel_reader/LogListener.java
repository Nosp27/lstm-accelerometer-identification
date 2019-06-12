package com.cw.nosp.android_accel_reader;

interface LogListener {
    void log(String s);
    void onRunningModeChanged(boolean newRunning);

    void onDataRecieved(boolean alright, float precision);

    void onDataRecorded(float ratio);
}