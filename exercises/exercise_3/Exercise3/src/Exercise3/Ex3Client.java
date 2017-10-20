package Exercise3;

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
public class Ex3Client {
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
            socket = new Socket("18.221.102.182", 38103);
            //output (TO SERVER)
            os = socket.getOutputStream();
            out = new PrintStream(os, true, "UTF-8");
            //input (FROM SERVER)
            is = socket.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            //userReader
            userIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connected to server.");
        } catch (Exception e) {
            System.out.println("Error in setting up connection to server: " + e);
        }
        int num = is.read();
	System.out.println("Reading " + num + " bytes");	
	int[] data = new int[num];
        System.out.println("Received bytes: ");
	for(int i = 0; i < num; i++) {
	//use input stream for reading bytes, isreader for reading characters
            if(i % 10 == 0) {
                if(i != 0) {
                    System.out.println("");
                }
                System.out.print(" ");
            }
            data[i] = is.read();
            System.out.print(Integer.toHexString(data[i]));
	}
        int realData[];
        //we get odd number of bytes from server-> 0 to last byte
        if(num % 2 == 1) {
            System.out.print(0);
            realData = new int[num+1];
            for (int i = 0; i < data.length; i++) {
                realData[i] = data[i];
            }
            realData[num] = 0;
        }
        else {
            realData = new int[num];
            for (int i = 0; i < data.length; i++) {
                realData[i] = data[i];
            }
        }
        System.out.println("\n");
        
        short checkSum = checksum(realData);
        System.out.println("Checksum calculated: 0x" + Integer.toHexString(checkSum));
        byte[] twoBytes = new byte[2];
        //number will be like 0xFFFF----
        twoBytes[0] = (byte)((checkSum >> 8) & 0xFF);
        twoBytes[1] = (byte)(checkSum & 0xFF);
        
        out.write(twoBytes);
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
    //java byte = [-128,127].... easier to use int array
    public static short checksum(int[] b) {
        int sum = 0;
        for(int i = 0; i < b.length; i += 2) {
            //each spot in the array represents a "byte". Shift the first 8, take the second 8 from next spot
            sum += (b[i] << 8) + b[i+1];
            if((sum & 0xFFFF0000) != 0) {
                sum &= 0xFFFF;
                sum++;
            }
        }
        return (short)~(sum & 0xFFFF);
    }
}
