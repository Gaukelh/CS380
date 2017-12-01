package exercise8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Hunter on Nov 30, 2017
 */
public class WebServer {
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8080);
            while(true) {
                try(Socket socket = server.accept()) {
                    //read from "client" or webbrowser
                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String url = br.readLine();
                    //split input string, we'll use [1]: "/hello.html"
                    String[] path = url.split("\\s+");
                    File file = new File("./www" + path[1]);
                    int response;
                    String response2;
                    //now that we have the path to our file, we'll check to see if file exists
                    if(file.exists()) {
                        response = 200;
                        response2 = " OK";
                    }
                    else {
                        response = 404;
                        response2 = " Not Found";
                        file = new File("./www/pagenotfound.html");
                    }
                    //allows us to print to output stream
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.println("HTTP/1.1 " + response + response2);
                    out.println("Content-type: text/html");
                    out.println("Content-length: " + file.length());
                    out.println();
                    //allows us to read from file
                    FileReader fr = new FileReader(file);
                    BufferedReader br2 = new BufferedReader(fr);
                    String line;
                    while((line = br2.readLine()) != null) {
                        out.println(line);
                    }
                    //this makes sure that whatever is written to the stream is actually written and not sitting in print writer's buffer
                    out.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
