
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
        } catch (Exception e) {
            System.out.println("Error in setting up connection to server: " + e);
        }
        System.out.println("Connected to server.");
        System.out.println("Received bytes: ");
        int test;
	test = isr.read();
	System.out.println("Reading " + test + " bytes");	
	int[] fromServer = new int[test];
	for(int i = 0; i < test; i++) {
	//use is for bytes, isr for not bytes(?) check documentation
		fromServer[i] = is.read();
	}
	/*

        out.write(b.array());
        if((int)isr.read() == 1) {
            System.out.println("Response Good");
        }
        else {
            System.out.println("Response Bad");
        }
        //close all input and out and socket connection
	*/
        is.close();
        isr.close();
        os.close();
        out.close();
        socket.close();
        System.out.println("Disconnected from Server");
    }
	public static short checksum(byte[] b) {

	}
}
