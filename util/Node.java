/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author omar
 */
public class Node {
     private Socket clientSocket;
     private  BufferedReader inFromNode;
    static DataOutputStream outNode;
    public Node(String moneda) throws IOException {
        String inputLine;
        String modifiedSentence;
        System.out.println("Conectando con Node");
        this.clientSocket = new Socket("127.0.0.1", 8000);
        this.outNode = new DataOutputStream(this.clientSocket.getOutputStream());
        outNode.writeUTF("{\"type\":CLIENT_TCP, \""+ moneda + "\"}");
        
        this.inFromNode = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        while ((inputLine = inFromNode.readLine()) != null) {
            System.out.println(inputLine);
        }
       
    }
        
}
