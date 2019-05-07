package com.cw.nosp.android_accel_reader;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GUI extends AppCompatActivity {
    static Random rand = new Random();

    private boolean running = true;

    private ServiceControl sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gui);
        (findViewById(R.id.transmit)).setVisibility(View.INVISIBLE);

        setStartStopClickListener();
        setEraseClickListener();
        setTransmitClickListener();

        sc = new ServiceControl();
        sc.initService(this, WriterService.class);

        getLogThread().start();
    }

    public void bindCallback(final String msg) {
        if (msg.startsWith("connection")) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    (findViewById(R.id.transmit)).setVisibility(msg.endsWith("estabilished") ? View.VISIBLE : View.INVISIBLE);
                    ((TextView) findViewById(R.id.net_debug)).setText(msg);
                }
            };
            (findViewById(R.id.transmit)).post(r);
        } else
            ((TextView) findViewById(R.id.text_pfc)).setText(String.format(Locale.ENGLISH, msg));
    }

    private void setStartStopClickListener() {
        (findViewById(R.id.startStop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View view) {
                if (!sc.isBound())
                    return;

                running = !running;
                ((TextView) findViewById(R.id.startStop)).setText(running ? "Стой" : "Пиши");
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
                    ((TextView) (findViewById(R.id.erase))).setText("Вы уверены?");

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
                                        ((TextView) findViewById(R.id.textView2)).setText(String.format(Locale.ENGLISH, "Размер: %.1fkB", size));
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

    String r(List<String> l) {
        return l.get(rand.nextInt(l.size()));
    }
}
