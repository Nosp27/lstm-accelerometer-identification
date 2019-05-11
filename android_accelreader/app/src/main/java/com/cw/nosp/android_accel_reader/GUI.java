package com.cw.nosp.android_accel_reader;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.w3c.dom.Text;

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

        setStartStopClickListener();
        setEraseClickListener();
        setTransmitClickListener();
        setIpFieldListener();

        sc = new ServiceControl();
        sc.initService(this, WriterService.class);

        getLogThread().start();
    }

    private void setIpFieldListener() {
        ((EditText) findViewById(R.id.ip_input)).setText("192.168.1.156");
        final Button ipButton = findViewById(R.id.changeIPBtn);
        ipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newAddr = ((TextView) findViewById(R.id.ip_input)).getText().toString();
                if (isValidIP(newAddr)) {
                    sc.changeIpAddress(newAddr);
                }
            }

            private boolean isValidIP(final String ip) {
                String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
                return ip.matches(PATTERN);
            }
        });
    }

    Vector<String> logQ = new Vector<>();

    @Override
    public void log(final String msg) {
        if (msg.startsWith("connection")) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    (findViewById(R.id.transmit)).setVisibility(msg.endsWith("established") ? View.VISIBLE : View.INVISIBLE);
                    ((TextView) findViewById(R.id.net_debug)).setText(msg);
                }
            };
            (findViewById(R.id.transmit)).post(r);
        } else {
            String s = "";
            logQ.add(msg);
            for (int i = 1; i <= Math.min(logQ.size(), 3); i++)
                s += logQ.get(logQ.size() - i) + "\n";
            final String _s = s;
            findViewById(R.id.text_pfc).post(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.text_pfc)).setText(String.format(Locale.ENGLISH, _s));
                }
            });
        }
    }

    @Override
    public void onChangedIp() {
        ((TextView) findViewById(R.id.net_debug)).setText("ip changed");
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
