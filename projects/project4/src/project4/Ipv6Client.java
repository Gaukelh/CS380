package project4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 *
 * @author Hunter on Nov 5, 2017
 */
class IPPacket {
    public static int ip_version;
    public static int payload_length;
    public static int next_header;
    public static int hop_limit;
    public static String source_address;
    public static String destination_address;
    public static final byte MAX = (byte)255;
    
    public static byte[] generatePacket () {
        byte packet[] = new byte[40 + payload_length];
        
        //IP version - 4 bits ([0].5)
        packet[0] = (byte)(ip_version << 4);
        //Traffic Class - 8 bits (this would be split between [0].5 and [1].5 array location)
        //not implemented
        //Flow Label - 20 bits ([1].5,[2],[3])
        //not implemented
        //Payload Length - 16 bit unsigned ([4],[5])
        packet[4] = (byte)(payload_length >>> 8);
        packet[5] = (byte)(payload_length);
        //Next header - 8 bit ([6])
        packet[6] = (byte)(next_header);
        //Hop Limit - 8 bit ([7])
        packet[7] = (byte)(hop_limit);
        //Source Address - 128 bits ([8] - [23]), 18-23 are used
        String[] s = source_address.split("\\.");
        packet[18] = MAX;
        packet[19] = MAX;
        for (int i = 0; i < s.length; i++) {
            int value = Integer.valueOf(s[i]);
            packet[i + 20] = (byte)value;
        }
        //Destination Address - 128 bits ([23] - [39]), 34-39 are used
        String[] d = destination_address.split("\\.");
        packet[34] = MAX;
        packet[35] = MAX;
        for (int i = 0; i < s.length; i++) {
            int value = Integer.valueOf(d[i]);
            packet[i + 36] = (byte)value;
        }
        return packet;
    }
}
public class Ipv6Client {
    
    public static void main(String[] args) throws IOException, UnknownHostException {
        Socket socket = new Socket("198.199.115.177", 80);
        
        OutputStream output_stream = socket.getOutputStream();
        InputStream input_stream = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(input_stream, "UTF-8");
        BufferedReader buffered_reader = new BufferedReader(isr);
        
        IPPacket p = new IPPacket();
        p.ip_version = 6;
        //UDP = 17
        p.next_header = 17;
        p.hop_limit = 20;
        p.source_address = "127.0.0.1";
        p.destination_address = socket.getInetAddress().getHostAddress();
        
        for (int i = 1; i <= 12; i++) {
            p.payload_length = (short)Math.pow(2, i);
            System.out.printf("Data length: %d\n", p.payload_length);
            
            byte[] packet = p.generatePacket();
            for(byte b : packet) {
                output_stream.write(b);
            }
            String output = "";
            for (int j = 0; j < 4; j++) {
                output += Integer.toHexString(input_stream.read());
            }
            System.out.print("Response: 0x" + output.toUpperCase() + "\n");
        }
    }
}