package com.cw.nosp.android_accel_reader;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.*;

import java.util.Locale;

public class WriterService extends Service implements DataTransmitter.ClientListener {

    //constants
    private static final int FOREGROUND_ID = 1000;
    private static final int KILOBYTE = 1024;
    /////////////////////////////

    //server-client connection
    private DataTransmitter dt;
    private Transmitter transmitter;
    private String currentIP = "192.168.1.156";
    /////////////////////////////

    //notifications
    private NotificationManagerCompat notMan;
    private NotificationCompat.Builder notificationBuilder;

    //File recording
    //get name of i-th file recorded
    private String filename(int i) {
        return String.format(Locale.ENGLISH, "%d.csv", i);
    }

    private FileWriter out;
    private BufferedWriter writer;
    private volatile File storage;
    private volatile int fileNum;
    /////////////////////////////

    //Accelerometer capture
    private SensorManager sm;
    private Sensor accelerometer;
    private volatile boolean running = true;
    /////////////////////////////

    //service control
    private IBinder localBinder = new LocalBinder();
    private boolean bound = false;
    private LogListener ll;
    /////////////////////////////

    //interface
    public void setLog(LogListener l) {
        ll = l;
    }
    /////////////////////////////

    /**
     * get size of file that recorder is working with
     *
     * @return size of used file in kilobytes
     */
    public synchronized double getSize() {
        if (running)
            updateNotification();
        return getSize0();
    }

    private synchronized double getSize0() {
        if (storage == null)
            return 0;
        return storage.length() * 1.0 / (8d * KILOBYTE);
    }

    /**
     * suspends service job. file recording stops until process resumed with {@code resume()} method
     */
    public synchronized void suspend() {
        if (!running)
            return;

        running = false;
        notMan.cancel(FOREGROUND_ID);
        dumpFileData();
        ll.log("Service is suspended");
        ll.onRunningModeChanged(false);
    }

    /**
     * resumes service job. recording continues
     *
     * @return true if service successfully been resumed, false if IO error occurred or process is already running
     */
    public synchronized boolean resume() {
        if (running)
            return false;

        updateNotification();
        try {
            createFile();
            ll.log("Service is running, file " + getFileNum());
            return true;
        } catch (IOException e) {
            ll.log("Error during creating file!");
            return false;
        } finally {
            running = true;
            setSensorData(new float[]{0, 0, 0});
            ll.onRunningModeChanged(true);
        }
    }

    /**
     * flushes and closes writing streams and buffers
     */
    private void dumpFileData() {
        try {
            writer.flush();
            writer.close();
            out.flush();
            out.close();
        } catch (IOException e) {

        }
    }

    //region service control
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (bound)
            return null;
        bound = true;

        try {
            createFile();
            initializeAccelerometer();
            captureSensorActivity();

            dt = new DataTransmitter(this, currentIP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        spawnNotification();
        return localBinder;
    }

    @Override
    public void onDestroy() {
        notMan.cancelAll();
        dumpFileData();
        super.onDestroy();
    }
    //endregion

    /**
     * Get the file index, where data recording
     *
     * @return number i of .../i.csv file, where accel data is being stored
     */
    public int getFileNum() {
        return fileNum;
    }

    /**
     * Method deletes all accelerometer recordings, stored in file system and begins new recording
     */
    public synchronized void deleteFiles() {
        suspend();

        File baseDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File[] existing = baseDir.listFiles();
        for (File f : existing)
            f.delete();

        fileNum = 0;
    }

    /**
     * Method suspends the recording and transmits data to server
     */
    public void transmitData() {
        if (transmitter == null)
            return;

        String baseDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        suspend();
        for (int i = 0; i < fileNum; i++) {
            transmitter.transmit(baseDir + File.separator + filename(i));
        }
    }

    //region server listener
    @Override
    public void onConnect(Transmitter t) {
        transmitter = t;
        ll.log("connection established");
    }

    @Override
    public void onDisconnect() {
        transmitter = null;
        ll.log("connection lost");
    }

    @Override
    public void onDataRecieved(boolean alright) {
        ll.onDataRecieved(alright);
    }

    @Override
    public void log(String s) {
        ll.log(s);
    }

    public void changeIp(String newValidIp, int port) {
        currentIP = newValidIp;
        dt.onChangeIp(currentIP, port);
        ll.onChangedIp();
    }
    //endregion

    /**
     * class-binder
     * used in binding service to mainframe
     */
    class LocalBinder extends Binder {
        WriterService get() {
            return WriterService.this;
        }
    }


    /**
     * method creates new csv file for recording accelerometer data
     *
     * @throws IOException
     */
    private void createFile() throws IOException {
        File baseDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        baseDir.mkdirs();

        File[] existing = baseDir.listFiles();
        while (!isUniqueFilename(existing))
            fileNum++;

        storage = new File(baseDir, filename(fileNum));
        out = new FileWriter(storage);
        writer = new BufferedWriter(out);
    }

    /**
     * checks if internal file counter is up to date
     *
     * @param existing csv files in storage directory
     * @return true if file counter is unique, false if file i.csv (where i is counter) exists
     */
    private boolean isUniqueFilename(File[] existing) {
        if (existing == null)
            return true;

        for (File f : existing)
            if (f.getName().equals(String.format(Locale.ENGLISH, "%d.csv", fileNum)))
                return false;
        return true;
    }

    /**
     * initialize sensor
     *
     * @throws IOException
     */
    private void initializeAccelerometer() throws IOException {
        try {
            sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } catch (NullPointerException e) {
            throw new IOException();
        }
    }

    //region sensor data access
    private final float[] sensorData = new float[3];//stores previously recorded coordinates

    /**
     * @return last coordinates captured from sensor
     */
    private float[] getSensorData() {
        synchronized (sensorData) {
            return sensorData;
        }
    }

    /**
     * remembers sensor data
     *
     * @param newData data to remember
     */
    private void setSensorData(float[] newData) {
        synchronized (sensorData) {
            for (int i = 0; i < 3; i++)
                sensorData[i] = newData[i];
        }
    }
    //endregion

    /**
     * initialize sensor listener and reading thread at rate of 20hz (every 50ms)
     */
    private void captureSensorActivity() {
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                setSensorData(sensorEvent.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        sm.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        storeAccelData(getSensorData());
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    /**
     * method adds data line to file
     *
     * @param data coordinates x,y,z
     * @throws IOException
     */
    private synchronized void storeAccelData(float[] data) throws IOException {
        if (!running)
            return;

        for (int i = 0; i < data.length; ++i) {
            writer.append(Float.toString(data[i]));
            if (i < data.length - 1)
                writer.append(",");
        }
        writer.append("\n");
        writer.flush();
    }

    /**
     * initializes notification manager and
     * creates notification, telling user that program running
     */
    private void spawnNotification() {
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setColor(0xff24f4b9);
        notificationBuilder.setContentTitle("Accelerometer");
        notificationBuilder.setContentText("Acceleration is being recorded");
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setTimeoutAfter(500);
        Intent activityIntent = new Intent(this, GUI.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        notMan = NotificationManagerCompat.from(getApplicationContext());
        notMan.notify(FOREGROUND_ID, notificationBuilder.build());
    }

    /**
     * respawns notification if it was closed
     */
    private void updateNotification() {
        notificationBuilder.setContentText("Acceleration is being recorded" + String.format(Locale.ENGLISH, "(%d.csv, %.1fkb)", fileNum, getSize0()));
        notificationBuilder.setTimeoutAfter(500);
        notMan.notify(FOREGROUND_ID, notificationBuilder.build());
    }
}
