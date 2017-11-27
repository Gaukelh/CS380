package project5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

class IPPacket {
    public static int ip_version;
    public static int header_length;
    public static int type_of_service;
    public static int data_length;
    public static int identification;
    public static int flag;
    public static int fragment_offset;
    public static int time_to_live;
    public static int protocol;
    public static int checksum;
    public static String source_address;
    public static String destination_address;
    public static byte[] data;

    public static byte[] generatePacket (byte[] data) {
        int data_length = data.length;
        byte packet[] = new byte[20 + data_length];

        packet[0] = (byte) ((ip_version << 4 & 0xF0) | (header_length & 0xF));
        packet[1] = (byte) type_of_service;
        packet[2] = (byte) (((20 + data_length) >> 8) & 0xFF); // Total Length (Upper)
        packet[3] = (byte) ((20 + data_length) & 0xFF); // Total Length (Lower)
        packet[4] = (byte) identification;
        packet[5] = (byte) identification;
        packet[6] = (byte) flag;
        packet[7] = (byte) fragment_offset;
        packet[8] = (byte) time_to_live;
        packet[9] = (byte) protocol;
        packet[10] = (byte) checksum;
        packet[11] = (byte) checksum;
        //source
        String[] s = source_address.split("\\.");
        for (int j = 0; j < s.length; j++) {
          int value = Integer.valueOf(s[j]);
          packet[j + 12] = (byte) value;
        }
        //dest
        String[] d = destination_address.split("\\.");
        for (int j = 0; j < d.length; j++) {
          int value = Integer.valueOf(d[j]);
          packet[j + 16] = (byte) value;
        }

        short checksum_value = checksum(packet);
        packet[10] = (byte) (checksum_value >> 8); // upper
        packet[11] = (byte) (checksum_value); // lower

        for (int i = 20; i < 20 + data_length; i++) {
            packet[i] = data[i-20];
        }
        return packet;
    }

    public static short checksum(byte[] b) {
        int sum = 0;

        for (int i = 0; i < b.length; i += 2) {

            // Convert to 16 bit
            sum += ((b[i] << 8 & 0xFF00) | (b[i + 1] & 0xFF));

            if ((sum & 0xFFFF0000) != 0) {
                /* carry occurred. so wrap around */
                sum &= 0xFFFF;
                sum++;
            }
        }
        return (short) ~(sum & 0xFFFF);
    }
}
//similar to ippacket
class UDPPacket {
    public static int source;
    public static int dest;
    public static byte[] data;
    public static String destination_address;
    public static String source_address;
    
    public static byte[] generatePacket (byte[] data) {
        byte[] packet = new byte[8 + data.length];
        
        packet[0] = (byte)(source >> 8);
        packet[1] = (byte)(source);
        packet[2] = (byte)(dest >> 8);
        packet[3] = (byte)(dest);
        packet[4] = (byte)(data.length >> 8);
        packet[5] = (byte)(data.length);

        //fill with data b4 header to get correct checksum
        for (int i = 8; i < 8 + data.length; i++) {
            packet[i] = data[i-8];
        }
        short checksum_value = checksum(generateHeader(packet));
        packet[6] = (byte)(checksum_value >> 8);
        packet[7] = (byte)(checksum_value);

        return packet;
    }
    public static byte[] generateHeader(byte[] data) {
        byte[] pseudo = new byte[12 + data.length];
        //source
        String[] s = source_address.split("\\.");
        for (int i = 0; i < s.length; i++) {
            int value = Integer.valueOf(s[i]);
            pseudo[i] = (byte) value;
        }
        //dest
        String[] d = destination_address.split("\\.");
        for (int i = 4; i < d.length + 4; i++) {
            int value = Integer.valueOf(d[i-4]);
            pseudo[i] = (byte) value;
        }
        pseudo[8] = 0;
        pseudo[9] = 17;
        pseudo[10] = data[4];
        pseudo[11] = data[5];
        
        for (int i = 12; i < pseudo.length; i++) {
            pseudo[i] = data[i-12];
        }
        return pseudo;
    }
    public static short checksum(byte[] b) {
        int sum = 0;

        for (int i = 0; i < b.length; i += 2) {

            // Convert to 16 bit
            sum += ((b[i] << 8 & 0xFF00) | (b[i + 1] & 0xFF));

            if ((sum & 0xFFFF0000) != 0) {
                /* carry occurred. so wrap around */
                sum &= 0xFFFF;
                sum++;
            }
        }
        return (short) ~(sum & 0xFFFF);
    }
}

public class UdpClient {

    public static void main (String[] args) throws IOException, UnknownHostException {
        Socket socket = new Socket("18.221.102.182", 38006);

        OutputStream output_stream = socket.getOutputStream();
        InputStream input_stream = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(input_stream, "UTF-8");
        BufferedReader buffered_reader = new BufferedReader(isr);

        System.out.println("Connected to server");
        
        //initial handshake
        byte[] data = {(byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF};
        IPPacket p = new IPPacket();
        p.ip_version = 4;
        p.header_length = 5;
        p.type_of_service = 0;
        p.identification = 0;
        p.flag = 64;
        p.fragment_offset = 0;
        p.time_to_live = 50;
        p.protocol = 17;
        p.checksum = 0;
        p.source_address = "127.0.0.1";
        p.destination_address = socket.getInetAddress().getHostAddress();
        output_stream.write(p.generatePacket(data));

        String output = "";
        for (int j = 0; j < 4; j++) {
            output += Integer.toHexString(input_stream.read());
        }
        System.out.print("Response: 0x" + output.toUpperCase() + "\n");
        byte[] port = new byte[2];
        input_stream.read(port);
        int destPort = (((port[0] &0xFF) << 8) | port[1] & 0xFF);
        //end handshake
        
        UDPPacket udp = new UDPPacket();
        udp.dest = destPort;
        udp.source = 1001;
        udp.destination_address = socket.getInetAddress().getHostAddress();
        udp.source_address = "127.0.0.1";
        
        //packets we will fill with udp packets
        IPPacket p2 = new IPPacket();
        p2.ip_version = 4;
        p2.header_length = 5;
        p2.type_of_service = 0;
        p2.identification = 0;
        p2.flag = 64;
        p2.fragment_offset = 0;
        p2.time_to_live = 50;
        p2.protocol = 17;
        p2.checksum = 0;
        p2.source_address = "127.0.0.1";
        p2.destination_address = socket.getInetAddress().getHostAddress();
        
        int rttTotal = 0;
        for (int i = 1; i <= 12; i++) {
            int d = (int) Math.pow(2, i);
            System.out.printf("Sending packet with %d bytes of data\n", d);
            
            //randomize the data that we send
            byte[] dataForUdp = new byte[d];
            for (int j = 0; j < dataForUdp.length; j++) {
                dataForUdp[j] = (byte)(Math.random() * 255);
            }
            
            output_stream.write(p2.generatePacket(udp.generatePacket(dataForUdp)));
            
            long rttInit = System.currentTimeMillis();
            String output1 = "";
            for (int j = 0; j < 4; j++) {
                output1 += Integer.toHexString(input_stream.read());
            }
            System.out.println("Response: 0x" + output1.toUpperCase());
            long rttEnd = System.currentTimeMillis();
            long rtt = rttEnd - rttInit;
            System.out.println("RTT: " + rtt);
            rttTotal += rtt;
        }
        System.out.println("Average RTT: " + (rttTotal/12));
        socket.close();
    }
}