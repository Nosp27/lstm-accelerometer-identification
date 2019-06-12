package com.cw.nosp.android_accel_reader;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;

import java.util.Locale;
import java.util.Vector;

public class GUI extends AppCompatActivity implements LogListener {
    private boolean running = true;
    private ServiceControl sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Android accelerometer verifier");
        this.setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar);
        setContentView(R.layout.activity_gui);
        (findViewById(R.id.transmit)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.verdict)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.ratingBar)).setVisibility(View.INVISIBLE);
        setStartStopClickListener();
        setEraseClickListener();
        setTransmitClickListener();
        sc = new ServiceControl();
        sc.initService(this, WriterService.class);

        getLogThread().start();
    }

    @Override
    public void log(final String msg) {
        findViewById(R.id.text_pfc).post(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.text_pfc)).setText(String.format(Locale.ENGLISH, msg));
            }
        });
    }

    @Override
    public void onRunningModeChanged(boolean newRunning) {
        if (running != newRunning)
            running = newRunning;
        ((TextView) findViewById(R.id.startStop)).setText(running ? "Suspend" : "Resume");
    }

    private void setStartStopClickListener() {
        (findViewById(R.id.startStop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View view) {
                if (!sc.isBound())
                    return;

                running = !running;
                onRunningModeChanged(running);
                if (running)
                    sc.resume();
                else sc.suspend();
            }
        });
    }

    private volatile boolean requested = false;

    private void setEraseClickListener() {
        (findViewById(R.id.erase)).setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View view) {
                if (!sc.isBound())
                    return;

                final int prevC = 0xffffbb33;
                final String oldText = ((TextView) (findViewById(R.id.erase))).getText().toString();

                if (!requested) {
                    requested = true;
                    (findViewById(R.id.erase)).setBackgroundColor(Color.RED);
                    ((TextView) (findViewById(R.id.erase))).setText("Are you sure?");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }

                            (findViewById(R.id.erase)).post(new Runnable() {
                                @Override
                                public void run() {
                                    (findViewById(R.id.erase)).setBackgroundColor(prevC);
                                    ((TextView) (findViewById(R.id.erase))).setText(oldText);
                                }
                            });
                            requested = false;
                        }
                    }).start();
                } else {
                    sc.suspend();
                    sc.deleteFiles();
                    requested = false;
                    (findViewById(R.id.erase)).setBackgroundColor(prevC);
                    ((TextView) (findViewById(R.id.erase))).setText(oldText);
                }
            }
        });
    }

    private void setTransmitClickListener() {
        (findViewById(R.id.transmit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View view) {
                sc.transmitData();
            }
        });
    }

    @Override
    public void onDataRecieved(final boolean alright, final float precision) {
        final String msg = String.format("P: %.2f", precision);
        findViewById(R.id.verdict).post(new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.verdict)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.verdict)).setText(String.format(Locale.ENGLISH, msg.substring(msg.indexOf(":") + 1)));
                (findViewById(R.id.ratingBar)).setVisibility(View.VISIBLE);
                if (alright) {
                    (((RatingBar) findViewById(R.id.ratingBar))).setRating(precision * 5);
                }
            }
        });

    }

    @Override
    public void onDataRecorded(final float ratio) {
        (findViewById(R.id.progressBar)).post(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) findViewById(R.id.progressBar)).setProgress((int) (ratio * 1000));
            }
        });
    }

    private Thread getLogThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        synchronized (this) {
                            if (sc.isBound()) {
                                final double size = sc.getSize();
                                findViewById(R.id.textView2).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((TextView) findViewById(R.id.textView2)).setText(String.format(Locale.ENGLISH, "Size: %.1fkB", size));
                                    }
                                });
                            }
                            wait(200);
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        });
    }
}
