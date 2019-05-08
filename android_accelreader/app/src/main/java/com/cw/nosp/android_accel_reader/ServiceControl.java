package com.cw.nosp.android_accel_reader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * This class provides service control.
 * Used for launching, stopping, binding and suspending/resuming the background service, that writes accelerometer data
 */
public class ServiceControl implements LogLostener {
    private WriterService service;
    private GUI theActivity;

    public void initService(GUI activity, Class<?> serviceClass) {
        theActivity = activity;
        Intent intent = new Intent(activity.getApplicationContext(), serviceClass);

        activity.bindService(intent, getSC(), Context.BIND_AUTO_CREATE);
    }

    public boolean isBound() {
        return service != null;
    }

    public void suspend() {
        service.suspend();
    }

    public void resume() {
        service.resume();
    }

    public double getSize() {
        return service.getSize();
    }

    private ServiceConnection getSC() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                WriterService.LocalBinder lb = (WriterService.LocalBinder) iBinder;
                service = lb.get();
                service.setLog(ServiceControl.this);
                theActivity.bindCallback("Служба привязана");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                service = null;
            }
        };
    }

    public void deleteFiles() {
        service.deleteFiles();
    }

    @Override
    public synchronized void log(String s) {
        theActivity.bindCallback(s);
    }

    public void transmitData() {
        service.transmitData();
    }
}

interface LogLostener{
    void log(String s);
}
