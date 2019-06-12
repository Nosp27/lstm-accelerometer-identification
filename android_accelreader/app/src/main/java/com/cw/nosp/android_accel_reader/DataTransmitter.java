package com.cw.nosp.android_accel_reader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class provides opportunity to transfer files to server via internet
 */
public class DataTransmitter implements Transmitter {
    private volatile String ipAddressString =
            "nospiy27.us-east-2.elasticbeanstalk.com/x";
    private volatile int port = 80;

    //for callback
    private volatile ClientListener l;

    //transmitter queue
    private final Queue<DataMap> q = new ConcurrentLinkedQueue<>();

    //thread is responsible for data transmission
    private Thread interactionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            rollInteraction();
        }
    }, "IT");

    /**
     * tries to connect to server
     *
     * @return boolean, representing the result of connection attempt
     */

    private synchronized void processConnect() {
        l.onConnect(this);
        notifyAll();
    }

    private synchronized void processDisconnect() {
        l.onDisconnect();
        notifyAll();
    }

    private void rollInteraction() {
        try {
            while (true) {
                try {
                    synchronized (DataTransmitter.this) {
                        DataMap data;
                        synchronized (q) {
                            while (q.isEmpty()) {
                                q.wait();
                            }
                            data = q.poll();
                        }
                        if (data == null)
                            continue;

                        HttpURLConnection clientConnection = getConnection(data);
                        processConnect();
                        transmit0(clientConnection, data);
                        receive0(clientConnection);
                    }
                } catch (IOException | NullPointerException e) {
                    l.log("e: " + Arrays.toString(e.getStackTrace()));
                }
            }
        } catch (InterruptedException e) {

        }
    }

    private HttpURLConnection getConnection(DataMap dataMap) throws IOException {
        URL url = new URL("http://" + ipAddressString);
        HttpURLConnection clientConnection;
        clientConnection = (HttpURLConnection) url.openConnection();
        clientConnection.setRequestMethod("POST");
        clientConnection.setRequestProperty("Host", url.getHost());
        clientConnection.setRequestProperty("Content-type", "text/plain");
        clientConnection.setRequestProperty("Content-Length", Integer.toString(dataMap.getSize()));
        clientConnection.setRequestProperty("Connection", "keep-alive");
        clientConnection.setDoOutput(true);
        return clientConnection;
    }

    private void transmit0(HttpURLConnection clientConnection, DataMap data) throws IOException {
        Date now = new Date();
        now.setTime(System.currentTimeMillis());
        l.log("transmitting " + data.getSize() + " data at " + now.toString());
        final OutputStream out = clientConnection.getOutputStream();
        out.write(data.toString().getBytes());
    }

    private void receive0(HttpURLConnection clientConnection) throws IOException {
        if (clientConnection.getResponseCode() == 200) {
            StringBuilder sb = new StringBuilder();
            InputStream in = clientConnection.getInputStream();
            int dataPiece;
            while ((dataPiece = in.read()) != -1) {
                sb.append((char) dataPiece);
            }
            Double v = Double.parseDouble(sb.substring(3));
            l.onDataRecieved(v < 0.5, (float) Math.abs(0.5 - v) * 2);
        }
        clientConnection.disconnect();
        processDisconnect();
    }
    //endregion

    /**
     * constructor. creates network client and adds client listener
     *
     * @param l listener
     */
    public DataTransmitter(ClientListener l, String ip) {
        if (ip != null)
            ipAddressString = ip;
        this.l = l;
    }

    /**
     * method begins transmission if it is not running already and
     * adds next filename to transmitter queue
     *
     * @param data Data Map object
     */
    public void transmit(final DataMap data) {
        synchronized (q) {
            if (q.isEmpty()) {
                q.add(data);
                if (interactionThread.getState() == Thread.State.NEW)
                    interactionThread.start();
                else q.notifyAll();
            } else {
                q.add(data);
                q.notifyAll();
            }
        }
    }

    /**
     * interface for callback-based interaction with client
     */
    public interface ClientListener {
        void onConnect(Transmitter t);

        void onDisconnect();

        void onDataRecieved(boolean alright, float precision);

        void log(String s);

    }

    public static void main(String[] args) {
        DataTransmitter dt = new DataTransmitter(new ClientListener() {
            @Override
            public void onConnect(Transmitter t) {
                System.out.println("cc");
            }

            @Override
            public void onDisconnect() {
                System.out.println("dc");
            }

            @Override
            public void onDataRecieved(boolean alright, float p) {
                System.out.println("rc: alright=" + alright);
            }

            @Override
            public void log(String s) {
                System.out.println(s);
            }
        }, null);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Scanner sc = new Scanner(System.in);
        while (true)
            switch (sc.next()) {
                case "t":
                    dt.transmit(new DataMap(Arrays.asList(
                            new Double[]{1d, 2d, 3d},
                            new Double[]{3d, 2d, 5d},
                            new Double[]{1d, 2d, 1d},
                            new Double[]{1d, 2d, 3d})));
                    break;
                case "f":
                    try {
                        dt.transmit(new DataMap(new File("C:\\Users\\Nosp\\IdeaProjects\\lstm-accelerometer-identification\\standalonedataprocess\\resources\\dataFromServer\\1.csv")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
    }
}
