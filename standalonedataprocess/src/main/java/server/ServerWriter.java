package server;

import configWork.ConfigManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class ServerWriter {
    ServerSocket ss;
    Socket clientSocket;

    int fileCounter = 0;

    Vector<File> writtenFiles = new Vector<>();
    private IServerDataAccessor serverModelAccessor;
    private ICallbackServer serverListener;

    private ServerWriter() {
        serverThread.start();
    }

    /**
     * accept client socket
     * @throws IOException
     */
    boolean initServer() {
        try {
            System.out.println("Server accepting...");
            ss = new ServerSocket(5000);
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
        } catch (IOException e) {
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

    Thread serverThread = new Thread(() -> {

        while (true) {
            if (!initServer()) {
                System.out.println("weak initialization occurred");
                continue;
            }

            processRequest();
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
                s += "Data trensfer: " + fileCounter + " files" + newline;
                s += "</html>";
                return s;
            }

            @Override
            public List<File> getSavedFiles() {
                return writtenFiles;
            }

            private String getServerState() {
                if (ss == null) return "is null";
                if (ss.isBound()) return "is bound";
                if (ss.isClosed()) return "is closed";
                return "?";
            }

            private String getConnectionState() {
                return (clientSocket != null ? (clientSocket.isConnected() ? "established" : "disconnected") : "not found");
            }
        };
    }

    public IServerDataAccessor getModelAccess() {
        if (serverModelAccessor == null)
            initServerModelAccessor();
        return serverModelAccessor;
    }

    public interface IServerDataAccessor {
        String getClientinfo();

        String getServerInfo();

        List<File> getSavedFiles();
    }

    public interface ICallbackServer {
        void onFileRecieved(File f);
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
