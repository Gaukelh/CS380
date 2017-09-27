package echoClientServer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public final class EchoServer {

    public static void main(String[] args) throws Exception {
        String ad = "ifyouseethisohno";
        try (ServerSocket serverSocket = new ServerSocket(22222)) {
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    //connection setup
                    String address = socket.getInetAddress().getHostAddress();
                    ad = address;
                    System.out.printf("Client connected: %s%n", address);
                    //output streams
                    OutputStream os = socket.getOutputStream();
                    PrintStream out = new PrintStream(os, true, "UTF-8");
                    out.printf("Hi %s, thanks for connecting!%n", address);
                    
                    //input streams
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    
                    String input;
                    while((input = br.readLine()) != null) {
                        out.printf("Server> %s%n", input);
                        if(input.equals("exit")){
                            break;
                        }
                    }
                    socket.close();
                    os.close();
                    is.close();
                    System.out.printf("Client disconnected: %s%n", address);
                } catch (Exception e) {
                    System.out.printf("Error or Client Disconnected: %s%n", ad);
                }
            }
        }
    }
}
