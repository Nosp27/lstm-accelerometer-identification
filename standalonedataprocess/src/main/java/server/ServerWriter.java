package server;

import configWork.ConfigManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWriter {
    ServerSocket ss;
    Socket clientSocket;

    int fileCounter = 0;

    private IListenServer serverModelAccessor;

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
                    case "str":
                        System.out.println("got message:");
                        System.out.println(r.readLine());
                        break;
                    case "file":
                        System.out.println("got file");
                        String dir = ConfigManager.loadProperty("loaded-data");
                        FileOutputStream fileOut = new FileOutputStream(new File(dir, "" + fileCounter++ + ".csv"));

                        String line = r.readLine();
                        do {
                            if (line.startsWith("end") || line.equals(""))
                                break;
                            fileOut.write((line + "\n").getBytes());
                        } while ((line = r.readLine()) != null);
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

    public static IListenServer createServer() {
        return new ServerWriter().getModelAccess();
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
        serverModelAccessor = new IListenServer() {
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

            private String getServerState() {
                if (ss == null) return "is null";
                if (ss.isBound()) return "is bound";
                if (ss.isClosed()) return "is closed";
                return "?";
            }

            private String getConnectionState() {
                return (clientSocket != null ? (clientSocket.isConnected() ? "estabilished" : "disconnected") : "not found");
            }
        };
    }

    public IListenServer getModelAccess() {
        if (serverModelAccessor == null)
            initServerModelAccessor();
        return serverModelAccessor;
    }

    public interface IListenServer {
        String getClientinfo();

        String getServerInfo();
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
