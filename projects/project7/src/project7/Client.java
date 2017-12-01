package project7;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 *
 * @author Hunter on Nov 28, 2017
 */
public class Client {
    
    String fileName;
    String hostAddress;
    int port;
    Key publicKey;
    
    public Client(String fileName, String hostAddress, int port) {
        this.fileName = fileName;
        this.hostAddress = hostAddress;
        this.port = port;
        try {
            publicKey = (PublicKey)new ObjectInputStream(new FileInputStream(fileName)).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    public void start() {
        try{
            Socket socket = new Socket(hostAddress, port);
            System.out.println("Connected to server: " + socket.getInetAddress());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //Generate an AES session key.
            KeyGenerator AESKeygen = KeyGenerator.getInstance("AES");
            //Encrypt the session key using the server’s public key. Use Cipher.WRAP_MODE to encrypt the key.
            Key sessionKey = AESKeygen.generateKey();
            Cipher c = Cipher.getInstance("RSA");
            //server's public key
            c.init(Cipher.WRAP_MODE, publicKey);
            //encrypted using wrap mode
            byte[] theKey = c.wrap(sessionKey);
            //so that we can try again
            boolean tryAgain = true;
            while(tryAgain) {
                //Prompt the user to enter the path for a file to transfer.
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter Path: ");
                String filePath = sc.nextLine();
                File checkFile = new File(filePath);
                if(!checkFile.exists()) {
                    System.out.println("File does not exist, prepare a file to tranfer");
                    throw new FileNotFoundException();
                }
                FileInputStream fis = new FileInputStream(checkFile);
                //If the path is valid, ask the user to enter the desired chunk size in bytes (default of 1024 bytes).
                System.out.print("Enter chunk size [1024]: ");
                int checkChunk = sc.nextInt();
                //After accepting the path and chunk size, send the server a StartMessage that contains the file name, length of the file in bytes, chunk size, and encrypted session key.
                StartMessage firstMessage = new StartMessage(filePath, theKey, checkChunk);
                oos.writeObject(firstMessage);
                //The server should respond with an AckMessage with sequence number 0 if the transfer can proceed, otherwise the sequence number will be -1
                AckMessage fromServer = (AckMessage)ois.readObject();
                int numChunks = (int)firstMessage.getSize()/firstMessage.getChunkSize();
                int sequenceNum = fromServer.getSeq();
                if(sequenceNum != -1) {
                    
                    System.out.println("Sending: " + filePath + " File size: " + checkFile.length());
                    System.out.println("Sending " + numChunks + " chunks.");
                    Cipher c2 = Cipher.getInstance("AES");
                    c2.init(Cipher.ENCRYPT_MODE, sessionKey);
                    byte[] data;
                    while(sequenceNum < numChunks){
                        //For each chunk, the client must first read the data from the file and store in an array based on the chunk size.  
                        data = new byte[firstMessage.getChunkSize()];
                        fis.read(data);
                        //It should then calculate the CRC32 value for the chunk.  
                        Checksum checksum = new CRC32();
                        checksum.update(data, 0, data.length);
                        
                        //Finally, encrypt the chunk data using the session key.  Note that the CRC32 value is for the plaintext of the chunk, not the ciphertext.
                        //c2 already using session key 
                        byte[] finalData = c2.doFinal(data);
                        Chunk chunkTemp = new Chunk(sequenceNum, finalData, (int)checksum.getValue());
                        oos.writeObject(chunkTemp);
                        AckMessage ackTemp = (AckMessage)ois.readObject();
                        //update sequenceNum for next chunk
                        sequenceNum = ackTemp.getSeq();
                        System.out.println("Chunks completed [" + sequenceNum + "/" + numChunks + "].");
                    }
                }
                else if(sequenceNum == -1) {
                    System.out.println("Server sent back ack with -1 sequenceNum");
                }
                else {
                    System.out.println("Server sent back ack with sequenceNum value: " + fromServer.getSeq());
                }
                System.out.print("Would you like to begin a new file transfer?(Y/N): ");
                //clears from nextInt line
                sc.nextLine();
                String again = sc.nextLine();
                if(!again.equalsIgnoreCase("y")) {
                    oos.close();
                    ois.close();
                    fis.close();
                    tryAgain = false;
                    DisconnectMessage bye = new DisconnectMessage();
                    oos.writeObject(bye);
                    socket.close();
                }                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
}
