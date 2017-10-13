package project2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author Hunter
 */
public class PhysLayerClient {
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
        try {
            socket = new Socket("18.221.102.182", 38002);
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
        double baseline = 0.0;
        for (int i = 0; i < 64; i++) {
            baseline += is.read();
        }
        baseline /= 64.0;
        System.out.println("Baseline established from preamble: " + baseline);
        //((32*8)/4)*5 =  the amount of unsigned bytes we get from server
        int[] fivebits = new int[320];
        for (int i = 0; i < fivebits.length; i++) {
            //using inputstreamreader gives us characters, we want the bytes of data
            if(is.read() > baseline) {
                fivebits[i] = 1;
            }
            else {
                fivebits[i] = 0;
            }
        }
        for (int i = 0; i < fivebits.length; i++) {
            System.out.println(fivebits[i]);
            
        }
        int[] fourbits = new int[320];
        //undo-ing NZRI
        
        
        

        if((int)isr.read() == 1) {
            System.out.println("Response Good");
        }
        else {
            System.out.println("Response Bad");
        }
        //close all input and out and socket connection
        is.close();
        isr.close();
        os.close();
        out.close();
        socket.close();
        System.out.println("Disconnected from Server");
    }
}
