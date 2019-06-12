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
public class ServiceControl {
    private WriterService service;
    private LogListener theActivity;

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
                service.setLog(theActivity);
                theActivity.log("Service is bound\n");
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

    public void transmitData() {
        service.transmitData();
    }
}
