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

        public static int decode(int in) {
        switch(in) {
            case 0b11110:
                return 0b0000;
            case 0b01001:
                return 0b0001;
            case 0b10100:
                return 0b0010;
            case 0b10101:
                return 0b0011;
            case 0b01010:
                return 0b0100;
            case 0b01011:
                return 0b0101;
            case 0b01110:
                return 0b0110;
            case 0b01111:
                return 0b0111;
            case 0b10010:
                return 0b1000;
            case 0b10011:
                return 0b1001;
            case 0b10110:
                return 0b1010;
            case 0b10111:
                return 0b1011;
            case 0b11010:
                return 0b1100;
            case 0b11011:
                return 0b1101;
            case 0b11100:
                return 0b1110;
            case 0b11101:
                return 0b1111;
            default:
                break;
        }
        return 0;
    }
        
    //read 64 bits from server, read 320 bits from server, decode 320 bits from nzri, decode from 4b/5b, send back as array
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
        //one less so we can figure out how to start the decode for NZRI
        for (int i = 0; i < 63; i++) {
            baseline += is.read();
        }
        int last = is.read();
        baseline += last;
        baseline /= 64.0;
        System.out.println("Baseline established from preamble: " + baseline);
        //((32*8)/4)*5 = 320, the amount of unsigned bytes we get from server
        int[] fiveBits = new int[320];
        for (int i = 0; i < fiveBits.length; i++) {
            //using inputstreamreader gives us characters, we want the bytes of data
            if(is.read() > baseline) {
                fiveBits[i] = 1;
            }
            else {
                fiveBits[i] = 0;
            }
        }
        //still 5 bits
        int[] nonNzri = new int[320];
        //undo-ing NZRI
        for (int i = 0; i < nonNzri.length; i++) {
            if(fiveBits[i] == last) {
                nonNzri[i] = 0;
            }
            else {
                nonNzri[i] = 1;
                last = fiveBits[i];
            }
        }
        // five bits to four bits
        // 320 bits = 32 * 10, each 10 bits are split
        //   ---------
        //   | 5 | 5 |   bits
        //   ---------
        // each byte will look like this^, (5 bits/half -> 4 bits, *2 = 8bits or 1 byte)
        // get first five bits, translate, get second five bits, combine (<<), translate, put into array
        // skip 10
        byte[] fourBits = new byte[32];
        for (int i = 0; i < nonNzri.length/10; i++) {
            int firstFive = 0;
            int secondFive = 0;
            for (int j = 0; j < 10; j++) {
                //shift one each time until we get 5
                if(j < 5) {
                    firstFive = (firstFive << 1) + nonNzri[(i * 10) + j];
                }
                else {
                    secondFive = (secondFive << 1) + nonNzri[(i * 10) + j];
                }
            }
            firstFive = decode(firstFive);
            secondFive = decode(secondFive);
            if(firstFive == Integer.MAX_VALUE|| secondFive == Integer.MAX_VALUE) {
                //this is coming up sometimes. I think it has to do with the last bit and the way I decode NZRI? Just a hunch...!
                System.out.println("Error in decoding 5b->4b");
                break;
            }
            fourBits[i] = (byte)((firstFive << 4) + secondFive);
        }
        System.out.print("Recieved 32 Bytes: ");
        for (int i = 0; i < fourBits.length; i++) {
            System.out.print(Integer.toHexString(fourBits[i]));
        }
        System.out.println("");
        out.write(fourBits);
        if((int)isr.read() == 1) {
            System.out.println("Response Good.");
        }
        else {
            System.out.println("Response Bad.");
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
