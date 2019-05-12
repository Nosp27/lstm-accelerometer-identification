package com.cw.nosp.android_accel_reader;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

/**
 * Class provides opportunity to transfer files to server via internet
 */
public class DataTransmitter implements Transmitter {
    private volatile Socket clientSocket;
    private volatile String ipAddressString;
    private volatile int port = 5000;

    private enum ConnectionState {
        CONNECTED,
        DISCONNECTED
    }

    private ConnectionState c_state = ConnectionState.DISCONNECTED;

    //for callback
    private volatile ClientListener l;

    //transmitter queue
    private final Vector<String> q = new Vector<>();

    //thread is responsible for establishing connection with server
    private Thread connectionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            connect();
        }
    }, "CT");

    //thread is responsible for data transmission
    private Thread transmissionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            rollTransmission();
        }
    }, "TT");

    //thread is responsible for data receiving
    private final Thread receiverThread = new Thread(new Runnable() {
        @Override
        public void run() {
            rollReceiving();
        }
    });

    //region runnables for threads
    private void connect() {
        try {
            do {
                Thread.sleep(600);

                if (!tryFindServer())
                    processDisconnect();

            } while (true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            processDisconnect();
        }
    }

    /**
     * tries to connect to server
     *
     * @return boolean, representing the result of connection attempt
     */
    private boolean tryFindServer() {
        try {
            synchronized (DataTransmitter.this) {
                while (isConnected())
                    return true;

                clientSocket = new Socket(ipAddressString, port);
                processConnect();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    private synchronized void processConnect() {
        if (c_state == ConnectionState.CONNECTED)
            return;

        c_state = ConnectionState.CONNECTED;
        l.onConnect(this);
        notifyAll();
        synchronized (receiverThread) {
            receiverThread.notifyAll();
        }
    }

    private synchronized void processDisconnect() {
        if (c_state == ConnectionState.DISCONNECTED)
            return;

        c_state = ConnectionState.DISCONNECTED;
        clientSocket = null;
        l.onDisconnect();
        notifyAll();
    }

    private void rollReceiving() {
        try {
            while (true) {
                BufferedReader br = null;
                try {
                    synchronized (receiverThread) {
                        if (clientSocket == null)
                            receiverThread.wait();
                        br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String line = br.readLine();
                        if (line == null || !line.equals("str"))
                            throw new IOException();

                        line = br.readLine();
                        l.onDataRecieved(line.startsWith("alright"));
                    }
                } catch (IOException e) {
                } catch (NullPointerException e) {
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {

        }
    }

    private void rollTransmission() {
        try {
            while (true) {
                try {

                    synchronized (q) {
                        if (!isConnected() || q.isEmpty()) {
                            q.wait();
                        }

                        //select file
                        String filename = q.get(0);
                        q.remove(0);

                        //init file reader
                        final FileReader in;
                        try {
                            in = new FileReader(new File(filename));
                        } catch (FileNotFoundException e) {
                            return;
                        }

                        //get outputStream
                        final OutputStream out = clientSocket.getOutputStream();
                        BufferedReader r = new BufferedReader(in);

                        out.write("file\n".getBytes());

                        String _line;

                        while ((_line = r.readLine()) != null)
                            out.write((_line + "\n").getBytes());


                        out.write("end\n".getBytes());
                    }
                } catch (IOException | NullPointerException e) {
                    processDisconnect();
                }
            }
        } catch (InterruptedException e) {
        }
    }
    //endregion

    /**
     * constructor. creates network client and adds client listener
     *
     * @param l listener
     */
    public DataTransmitter(ClientListener l, String ip) {
        ipAddressString = ip;
        this.l = l;
        connectionThread.start();
        receiverThread.start();
    }

    /**
     * method begins transmission if it is not running already and
     * adds next filename to transmitter queue
     *
     * @param line filename
     * @return string, representing result of operation
     */
    public void transmit(final String line) {
        synchronized (q) {
            if (q.isEmpty()) {
                q.add(line);
                if (c_state == ConnectionState.CONNECTED && transmissionThread.getState() == Thread.State.NEW)
                    transmissionThread.start();
                else q.notifyAll();
            } else {
                q.add(line);
                q.notifyAll();
            }
        }
    }

    @Override
    public void onChangeIp(String newValidIp, int newPort) {
        synchronized (DataTransmitter.this) {
            ipAddressString = newValidIp;
            port = newPort == 0 ? port : newPort;
            if (isConnected()) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    /**
     * interface for callback-based interaction with client
     */
    public interface ClientListener {
        void onConnect(Transmitter t);

        void onDisconnect();

        void onDataRecieved(boolean alright);

        void log(String s);

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
                public void onDataRecieved(boolean bool) {
                    System.out.println("received: " + (bool ? "ok" : "!ok"));
                }

                @Override
                public void log(String s) {
                    System.out.println("log: " + s);
                }
            }, "192.168.1.156");


//            dt.transmit("C:\\Users\\Nosp\\IdeaProjects\\android_accelreader\\app\\app.iml");
            System.out.println("tx");

            Scanner sc = new Scanner(System.in);
            while (true) {
                switch (sc.next()) {
                    case "t":
                        dt.transmit("C:\\Users\\Nosp\\IdeaProjects\\android_accelreader\\app\\app.iml");
                        break;
                    case "c":
                        dt.onChangeIp("192.168.1.155", 0);
                        break;
                    case "cb":
                        dt.onChangeIp("192.168.1.156", 0);
                        break;
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
