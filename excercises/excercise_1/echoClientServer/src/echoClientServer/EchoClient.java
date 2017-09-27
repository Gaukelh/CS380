package echoClientServer;


import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public final class EchoClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 22222)) {
            String exit = "exit";
            //output to server
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");
            //input from server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            System.out.println(br.readLine());
            //userInput
            BufferedReader userReader = new BufferedReader(new InputStreamReader(System.in));
            String input;
            System.out.print("Client> ");
            while((input = userReader.readLine()) != null) {
                if(input.equals(exit)) {
                    //rather hacky... socket.close() was not giving me what I wanted
                    System.exit(0);
                }
                out.printf("%s%n", input);
                System.out.println(br.readLine());
                System.out.print("Client> ");
            }
        }
    }
}