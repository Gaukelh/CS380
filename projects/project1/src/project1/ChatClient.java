/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;

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
            socket = new Socket("18.221.102.182", 38001);
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
        Runnable serverOutputThread = () -> {
            String serverOutput;
            try {
                while((serverOutput = br.readLine()) != null) {
                    System.out.println(serverOutput);
                }
            } catch (Exception e) {
                System.out.println("Error in serverOutputThread creation: " + e);
            }
        }; 
        String userInput;
        try {
            Thread serverOut = new Thread(serverOutputThread);
            serverOut.start();
            System.out.print("Enter a username: ");
            while((userInput = userIn.readLine()) != null) {
                System.out.println("");
                //out to server
                out.printf("%s%n", userInput);
                if(userInput.equals("exit")) {
                    break;
                }
            }
            //close all input and out and socket connection
            is.close();
            isr.close();
            os.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Error in userInput/thread creation block: " + e);
        }
    }
}
