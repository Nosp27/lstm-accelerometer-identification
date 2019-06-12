package server;

import configWork.ConfigManager;
import sun.rmi.transport.tcp.TCPConnection;
import sun.rmi.transport.tcp.TCPEndpoint;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class ServerWriter {
    private ServerSocket ss;
    private Socket clientSocket;
    private int fileCounter = 0;
    private Vector<File> writtenFiles = new Vector<>();
    private IServerDataAccessor serverModelAccessor;
    private ICallbackServer serverListener;

    private ServerWriter() {
        try {
            int port = Integer.parseInt(ConfigManager.loadProperty("port"));
            ss = new ServerSocket(port, 1);
            serverThread.start();
            responseThread.start();
        } catch (IOException e) {
            ss = null;
        }
    }

    /**
     * accept client socket
     *
     * @throws IOException
     */
    boolean initServer() {
        try {
            System.out.println("Server accepting...");
            clientSocket = ss.accept();
            System.out.println("Accepted " + clientSocket.toString());

            return true;
        } catch (IOException e) {
            return false;
        }
    }


    //implement and replace
    private void processRequest() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            while (true) {
                System.out.println("Wait for content");
                String classLine;
                classLine = r.readLine();

                System.out.println("read class line");

                switch (classLine) {
                    case "str"://echo
                        System.out.println("got message:");
                        System.out.println(r.readLine());
                        break;
                    case "file":
                        System.out.println("got file");
                        String dir = ConfigManager.loadProperty("loaded-data");
                        File destinationFile = new File(dir, "" + getFileCounter(new File(dir)) + ".csv");
                        System.out.println(destinationFile.getAbsolutePath());
                        FileOutputStream fileOut = new FileOutputStream(destinationFile);

                        String line = r.readLine();
                        do {
                            if (line.startsWith("end") || line.equals(""))
                                break;
                            fileOut.write((line + "\n").getBytes());
                        } while ((line = r.readLine()) != null);
                        fileOut.close();

                        writtenFiles.add(destinationFile);
                        serverListener.onFileRecieved(destinationFile);//send message
                        break;
                    default:
                        System.out.println("got strange class message:");
                        System.out.println(r.readLine());
                        break;

                }
            }
        } catch (IOException | NullPointerException e) {
            //client disconnected
            e.printStackTrace();
        }
    }

    private int getFileCounter(File dir) {
        while (new File(dir, fileCounter + ".csv").exists())
            fileCounter++;
        return fileCounter;
    }

    public static IServerDataAccessor createServer(ICallbackServer serverListener) {
        ServerWriter sw = new ServerWriter();
        sw.serverListener = serverListener;

        return sw.getModelAccess();
    }

    private Thread serverThread = new Thread(() -> {

        try {
            while (true) {
                if (!initServer()) {
                    System.out.println("weak initialization occurred");
                    continue;
                }

                processRequest();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    });

    private final ResponseSyncronizer responseThreadLock = new ResponseSyncronizer(false);
    private Thread responseThread = new Thread(() -> {
        try {
            while (true) {
                synchronized (responseThreadLock) {
                    responseThreadLock.wait();

                    try {
                        sendResponse0(responseThreadLock.getState());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (InterruptedException e) {

        }
    });

    private void initServerModelAccessor() {
        serverModelAccessor = new IServerDataAccessor() {
            @Override
            public String getClientinfo() {
                String newline = "<br>";
                String s = "";
                s += "<html>";
                s += "Client " + getConnectionState() + newline;
                s += "</html>";
                return s;
            }

            @Override
            public String getServerInfo() {
                String newline = "<br>";
                String s = "";
                s += "<html>";
                s += "Server" + newline;
                s += "State: " + getServerState() + newline;
                s += "Connection: " + getConnectionState() + newline;

                try {
                    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                    while (e.hasMoreElements()) {
                        Enumeration<InetAddress> ee = e.nextElement().getInetAddresses();
                        while (ee.hasMoreElements()) {
                            InetAddress addr = ee.nextElement();
                            if (addr.getAddress().length == 4)
                                s += "IP: " + addr.getHostAddress() + newline;
                        }
                    }
                } catch (SocketException e) {
                }

                s += "Last file located: " + (fileCounter) + ".csv" + newline;
                s += "</html>";
                return s;
            }

            @Override
            public List<File> getSavedFiles() {
                return writtenFiles;
            }

            @Override
            public void sendResponse(boolean userChanged) {
                synchronized (responseThreadLock) {
                    responseThreadLock.setState(userChanged);
                    responseThreadLock.notifyAll();
                }
            }

            private String getServerState() {
                if (ss == null) return "is null";
                if (ss.isBound()) return "is bound";
                if (ss.isClosed()) return "is closed";
                return "?";
            }

            private String getConnectionState() {
                if (clientSocket == null)
                    return "not found";
                if (!clientSocket.isClosed())
                    return "established";
                return "disconnected";
            }
        };
    }

    public void sendResponse0(boolean hasChanged) throws IOException {
        if (clientSocket == null || !(clientSocket.isConnected() && !clientSocket.isClosed()))
            return;
        OutputStream out = clientSocket.getOutputStream();
        String response = "";
        response += "str\n";
        response += !hasChanged ? "alright" : "changed" + "\n";
        out.write(response.getBytes());
    }

    private IServerDataAccessor getModelAccess() {
        if (serverModelAccessor == null)
            initServerModelAccessor();
        return serverModelAccessor;
    }

    public interface IServerDataAccessor {
        String getClientinfo();

        String getServerInfo();

        List<File> getSavedFiles();

        void sendResponse(boolean userChanged);
    }

    public interface ICallbackServer {
        void onFileRecieved(File f);
    }

    private class ResponseSyncronizer {
        boolean state;

        public ResponseSyncronizer(boolean state) {
            this.state = state;
        }

        public synchronized void setState(boolean state) {
            this.state = state;
        }

        public synchronized boolean getState() {
            return state;
        }
    }
}
