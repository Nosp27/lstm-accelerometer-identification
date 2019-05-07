package com.cw.nosp.android_accel_reader;

import java.io.*;
import java.net.Socket;
import java.util.Vector;

/**
 * Class provides opportunity to transfer files to server via internet
 */
public class DataTransmitter implements Transmitter {
    private volatile Socket clientSocket;

    //for callback
    private volatile ClientListener l;

    //transmitter queue
    private volatile Vector<String> q;

    //thread is responsible for establishing connection with server
    private Thread connectionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!tryFindServer());

            synchronized (DataTransmitter.this) {
                l.onConnect(DataTransmitter.this);
                DataTransmitter.this.notifyAll();
            }

            while (true) {
                try {
                    synchronized (this) {
                        InputStream in = clientSocket.getInputStream();
                        if (in.available() > 0) {
                            l.onDataRecieved(in);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }, "CT");

    //thread is responsible for data transmission
    private Thread transmissionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            synchronized (DataTransmitter.this) {
                try {
                    while (true) {
                        if (clientSocket == null)
                            DataTransmitter.this.wait();

                        final FileReader in;

                        if (q.size() == 0) {
                            DataTransmitter.this.wait();
                        }

                        String filename = q.get(0);
                        q.remove(0);

                        try {
                            in = new FileReader(new File(filename));
                        } catch (FileNotFoundException e) {
                            return;
                        }

                        final OutputStream out = clientSocket.getOutputStream();
                        BufferedReader r = new BufferedReader(in);

                        out.write("file\n".getBytes());

                        String _line;

                        while ((_line = r.readLine()) != null)
                            out.write((_line + "\n").getBytes());


                        out.write("end\n".getBytes());
                    }
                } catch (IOException e) {
                    l.onDisconnect();
                } catch (InterruptedException e) {
                }
            }
        }
    }, "TT");

    /**
     * tries to connect to server
     * @return boolean, representing the result of connection attempt
     */
    private synchronized boolean tryFindServer() {
        try {
            clientSocket = new Socket("192.168.1.156", 5000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * constructor. creates network client and adds client listener
     * @param l listener
     */
    public DataTransmitter(ClientListener l) {
        this.l = l;
        connectionThread.start();
    }

    /**
     * method begins transmission if it is not running already and
     * adds next filename to transmitter queue
     * @param line filename
     * @return string, representing result of operation
     */
    public synchronized String transmit(final String line) {
        if (q == null) {
            q = new Vector<>();
            q.add(line);
            transmissionThread.start();
        } else {
            q.add(line);
            notifyAll();
        }
        return "usual success";
    }

    /**
     * interface for callback-based interaction with client
     */
    public interface ClientListener {
        void onConnect(Transmitter t);

        void onDisconnect();

        void onDataRecieved(InputStream in);

    }

    public static void main(String[] args) {
        try {
            DataTransmitter dt = new DataTransmitter(new ClientListener() {
                @Override
                public void onConnect(Transmitter t) {
                    System.out.println("connected");
                }

                @Override
                public void onDisconnect() {
                    System.out.println("disconnected");
                }

                @Override
                public void onDataRecieved(InputStream in) {
                    System.out.println("received");
                }
            });

            for (int i = 0; i < 10; i++) {
                dt.transmit("C:\\Users\\Nosp\\IdeaProjects\\android_accelreader\\app\\app.iml");
            }
        } catch (RuntimeException e) {
        }
    }
}
