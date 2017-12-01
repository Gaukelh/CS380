package project7;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

/**
 *
 * @author Hunter on Nov 28, 2017
 */
public class FileTransfer {
    
    public static void main(String[] args) {
        //because I use an ide
        /*Scanner tester = new Scanner(System.in);
        System.out.println("S/C: ");
        String input = tester.nextLine();
        if(input.equals("server")) {
            Server s = new Server("private.bin", 38007);
            s.start();
        }
        else if(input.equals("client")) {
            Client c = new Client("public.bin", "localhost", 38007);
            c.start();
        }
        else if(input.equals("makeKey")) {
            makeKeys();
        }*/
        
        
        
        if(args.length > 0) {
            if(args[0].equals("server")) {
                Server s = new Server(args[1], Integer.parseInt(args[2]));
                s.start();
            }
            else if (args[0].equals("client")) {
                Client c = new Client(args[1], args[2], Integer.parseInt(args[3]));
                c.start();
                
            }
            else if(args[0].equals("makekeys")) {
                makeKeys();
            }
            else {
                System.out.println("Plz try again");
            }
        }
        else {
            System.out.println("Please try again");
        }
    }
    public static void makeKeys() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(4096); // you can use 2048 for faster key generation
            KeyPair keyPair = gen.genKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(new File("public.bin")))) {
                oos.writeObject(publicKey);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(new File("private.bin")))) {
                oos.writeObject(privateKey);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
