/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exercise2;

/**
 *
 * @author Hunter
 */
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
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
        int test;
        int test2;
        String comp = "";
        byte[] data = new byte[100];
        for (int i = 0; i < 100; i++) {
            comp = "";
            test = isr.read();
            test2 = isr.read();
            comp = Integer.toHexString(test) + Integer.toHexString(test2);
            data[i] = (byte)Integer.parseInt(comp, 16);
            
            if(i%10 == 0) {
                System.out.println("");
            }
            System.out.println(comp);
        }
        for (int i = 0; i < data.length; i++) {
            System.out.println("data[i] " + data[i]);
            
        }
        byte[] realData = new byte[100];
        for (int i = 0; i < data.length; i++) {
            realData[i] = (byte)data[i];
            System.out.println("realData: " + realData[i]);
            
        }
        
        CRC32 thirtyTwo = new CRC32();
        thirtyTwo.update(realData);
        
        
        int ret = (int)thirtyTwo.getValue();
        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
        b.putInt(ret);

        out.write(b.array());
        System.out.println(isr.read());
        
        
        TimeUnit.SECONDS.sleep(2);
        //close all input and out and socket connection
        is.close();
        isr.close();
        os.close();
        out.close();
        socket.close();
    }
}