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
        try (ServerSocket serverSocket = new ServerSocket(22222)) {
            while (true) {
                try {
                    //connection setup
                    final Socket socket = serverSocket.accept();
                    String address = socket.getInetAddress().getHostAddress();
                    System.out.printf("Client connected: %s%n", address);
                    Runnable ser = () -> {
                        try {
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
                            //close down the connections
                            socket.close();
                            os.close();
                            is.close();
                            System.out.printf("Client disconnected: %s%n", address);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    };
                    Thread serThread = new Thread(ser);
                    serThread.start();
                } catch (Exception e) {
                    System.out.println("Error or Client Disconnected");
                }
            }
        }
    }
}
