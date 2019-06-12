package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer extends Thread {
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        new TestServer().start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(5000);


            while (true) {
                try {
                    new Connection(serverSocket.accept());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Connection extends Thread {
        private Socket s;

        public Connection(Socket s) {
            this.s = s;
            start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (s.isConnected() && !s.isClosed() && !s.isOutputShutdown()) {
                        s.getOutputStream().write("HTTP/1.1 200 OK\n".getBytes());
                        System.out.println("Roll echo: " + s.getInetAddress().getHostAddress());
                        Thread.sleep(1000);
                    }
                }
            } catch (IOException e) {
                System.out.println("quit");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
