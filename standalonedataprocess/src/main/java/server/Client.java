package server;

import akka.io.Inet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client {
    public static void main(String[] args) {
        try {
            String host = "LstmRecognizer-env.mmbtxj5scy.us-east-2.elasticbeanstalk.com";
            Socket s = new Socket(host, 80);
            System.out.println("!");
            s.getOutputStream().write("GET http://LstmRecognizer-env.mmbtxj5scy.us-east-2.elasticbeanstalk.com HTTP/1.1\n".getBytes());

            char c;
            do{
                c = (char) s.getInputStream().read();
                System.out.print(c);
            }
            while (c != '\n');
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
