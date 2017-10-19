/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Hunter
 */
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

/**
 *
 * @author Hunter
 */
public class ChatClient {
    //socket
    private static Socket socket = null;
    //Output (what we send to server)
    private static OutputStream os = null;
    private static PrintStream out = null;
    //input stream (what we get from server)
    private static InputStream is = null;
    private static InputStreamReader isr = null;
    private static BufferedReader br = null;
    //userInput
    private static BufferedReader userIn = null;

    public static void main(String[] args) throws Exception {
        //open socket, input and output streams, and userInput stream
	System.out.println("Hello1");
        try {
	System.out.println("asdf");
            socket = new Socket("18.221.102.182", 38103);
	System.out.println("asdf414123");
            //output (TO SERVER)
            os = socket.getOutputStream();
            out = new PrintStream(os, true, "UTF-8");
            //input (FROM SERVER)
            is = socket.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            //userReader
            userIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            System.out.println("Error in setting up connection to server: " + e);
        }
	System.out.println("Hello");
	int testtest = is.read();
	System.out.println("First data: " + testtest);


        TimeUnit.SECONDS.sleep(2);
        //close all input and out and socket connection
        is.close();
        isr.close();
        os.close();
        out.close();
        socket.close();
    }
}
