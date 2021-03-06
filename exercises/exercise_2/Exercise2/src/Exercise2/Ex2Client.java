package Exercise2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 *
 * @author Hunter
 */
public class Ex2Client {
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
            socket = new Socket("18.221.102.182", 38102);
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
        System.out.println("Connected to server.");
        System.out.println("Received bytes: ");
        int test;
        int test2;
        String comp = "";
        byte[] data = new byte[100];
        for (int i = 0; i < 100; i++) {
            comp = "";
            test = isr.read();
            test2 = isr.read();
            comp = Integer.toHexString(test) + Integer.toHexString(test2);
            if(i%10 == 0) {
                System.out.print(" ");
            }
            System.out.print(comp);
            if(i%10 == 9) {
                System.out.println("");
            }
            data[i] = (byte)Integer.parseInt(comp, 16);
        }
        
        CRC32 thirtyTwo = new CRC32();
        thirtyTwo.update(data);
        int ret = (int)thirtyTwo.getValue();
        System.out.println("Generated CRC32: " + Integer.toHexString(ret));
        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
        b.putInt(ret);

        out.write(b.array());
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