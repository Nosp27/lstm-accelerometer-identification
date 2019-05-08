package com.cw.nosp.android_accel_reader;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
        setIpFieldListener();

        sc = new ServiceControl();
        sc.initService(this, WriterService.class);

        getLogThread().start();
    }

    private void setIpFieldListener() {
        final EditText ipEditor = findViewById(R.id.ip_input);
        ipEditor.addTextChangedListener(new TextWatcher() {
            String previousText;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                previousText = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(isValidIP(ipEditor.getText().toString())){

                }
            }

            private boolean isValidIP(final String ip) {
                String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
                return ip.matches(PATTERN);
            }
        });
    }

    public void bindCallback(final String msg) {
        if (msg.startsWith("connection")) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    (findViewById(R.id.transmit)).setVisibility(msg.endsWith("established") ? View.VISIBLE : View.INVISIBLE);
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
                ((TextView) findViewById(R.id.startStop)).setText(running ? "Suspend" : "Resume");
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

    String r(List<String> l) {
        return l.get(rand.nextInt(l.size()));
    }

    public interface ServerListener{
        void onChangeIp(String newValidIp);
    }
}
