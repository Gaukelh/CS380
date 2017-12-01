package project7;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.crypto.Cipher;

/**
 *
 * @author Hunter on Nov 28, 2017
 */
public class Server {

    
    int port;
    String fileName;
    public Server(String fileName, int port) {
        this.fileName = fileName;
        this.port = port;
    }
    
    public void start() {
        try{
            //start up server and streams
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            FileOutputStream fos;

            //variables we can use across the different message types
            int chunk = 0, sequenceNum = -1;
            //init as null so java doesn't yell at me
            Key sessionKey = null;
            Message theMessage;
            boolean disconnectMessage = true;
            
            //while we get packets from client
            //break down based on packet types
            while(disconnectMessage) {
                theMessage = (Message)ois.readObject();
                
                //close all streams, sockets, and end the while loop
                if(theMessage.getType().equals(MessageType.DISCONNECT)) {
                    oos.close();
                    ois.close();
                    socket.close();
                    serverSocket.close();
                    disconnectMessage = false;
                }
                //Stop transfering data
                else if(theMessage.getType().equals(MessageType.STOP)) {
                    //discard associated filetransfer, respond with ackMessage, sequenceNum = -1
                    sequenceNum = -1;
                    AckMessage toWrite = new AckMessage(-1);
                    oos.writeObject(toWrite);
                } 
                
                //prepare for file transfer
                else if(theMessage.getType().equals(MessageType.START)) {
                    try {
                        sequenceNum = 0;
                        StartMessage startMessage = (StartMessage)theMessage;
                        chunk = (int) startMessage.getSize() / startMessage.getChunkSize();
                        
                        FileInputStream toGetFileInput = new FileInputStream(fileName);
                        ObjectInputStream fileInput = new ObjectInputStream(toGetFileInput);
                        
                        //decrypt to private key
                        Key privateKey = (Key)fileInput.readObject();
                        //because we make RSA keys in makeKeys() of FileTransfer.java
                        Cipher c = Cipher.getInstance("RSA");

                        //initialize c to unwrap mode with privateKey
                        c.init(Cipher.UNWRAP_MODE, privateKey);
                        byte[] temp = startMessage.getEncryptedKey();
                        //initialize session key for Chunk
                        sessionKey = c.unwrap(temp, "AES", Cipher.SECRET_KEY);
                        AckMessage toWrite = new AckMessage(sequenceNum);
                        
                        oos.writeObject(toWrite);
                    } catch(Exception e) {
                        // If the server is unable to begin the file transfer it should respond with an AckMessage with sequence number -1.
                        sequenceNum = -1;
                        oos.writeObject(new AckMessage(sequenceNum));
                        //e.printStackTrace();
                    }
                } 
                //recieve chunk from client
                else if(theMessage.getType().equals(MessageType.CHUNK)) {
                    Chunk chunkMessage = (Chunk)theMessage;
                    int chunkNum = chunkMessage.getSeq();
                    //The Chunk's sequence number must be the next expected sequence number by the server
                    if(sequenceNum == chunkNum) {
                        Cipher c = Cipher.getInstance("AES");
                        //decrypt  the  data  stored  in  the Chunk using  the  session  key  from  the transfer initialization step
                        c.init(Cipher.DECRYPT_MODE, sessionKey);
                        //doFinal = decrypts data for us
                        byte[] data = c.doFinal(chunkMessage.getData());
                        Checksum checksum = new CRC32();
                        //Next, the server should calculate the CRC32 value for the decrypted data
                        checksum.update(data, 0, data.length);
                        //and compare it with the CRC32 value included in the chunk
                        int crcValue = (int)checksum.getValue();
                        int chunkValue = chunkMessage.getCrc();
                        if(crcValue == chunkValue) {
                            //if these values match and the sequence number of the chunk is the next expected sequence number,
                            //the  server  should  accept  the  chunk  by  storing  the  data  and  incrementing  the  next  expected
                            //sequence number.
                            sequenceNum++;
                            try {
                                fos = new FileOutputStream("test2.txt");
                                fos.write(data);
                                System.out.println("Chunk recived [" + sequenceNum + "/" + chunk + "].");
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                            oos.writeObject(new AckMessage(sequenceNum));
                        }
                    }
                    if(sequenceNum == chunk) {
                        //reset file transfer
                        chunk = 0;
                        sequenceNum = -1; 
                        sessionKey = null;
                        System.out.println("Transfer Complete.");
                        System.out.println("Output path: test2.txt");
                    }
                }
                
                
            }
                    
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
