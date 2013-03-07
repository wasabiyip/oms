/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oms.deliverer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author omar
 */
public class MsjStreaming extends Thread {
    private Socket socket;
    private BufferedReader inFromNode;
    private DataOutputStream outNode;
    @Override
    public void run(){
        try {
            this.socket = new Socket("127.0.0.1",3000);
            this.outNode = new DataOutputStream(socket.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
            StringBuffer msjin = new StringBuffer();
            int c;
            while (isr.read() > 0) {
                while ((c = isr.read()) != 10) {
                    msjin.append((char) c);
                   
                }
                //Evaluamos cada cadena recibida.
                this.inHandler(msjin.toString());
                //borramos el contenido pa' que no se acumule...
                msjin.delete(0, msjin.length());
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(SenderApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SenderApp.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    public void login(String profile){
        
        this.writeNode("{\"type\": \"login\", "
                    + "\"name\":\"app\", "
                    + "\"profile\":\""+ profile +"\""
                    + "}");
    }
    /**
     * Notificamos que modificamos/añadimos Sl-Tp de una orden.
     * @param orden 
     */
    public void modOrden(Orden orden){
        
            this.writeNode("{"
                +"\"type\":\"orderModify\","
                +"\"data\":"
                +"{"
                    +"\"id\":\""+orden.getId()+"\","
                    +"\"nwSl\":"+orden.getSl()+","
                    +"\"nwTp\":"+orden.getTp()+","
                +"}"
            +"}");
    }
    /**
     * Notificamos que cerro una orden.
     * @param orden 
     */
    public void clOrden(Orden orden){
        
        this.writeNode("{"        
            +"\"type\":\"onOrderClose\","
            +"\"data\":"
            +"{"
                +"\"id\":\""+orden.getId()+"\""
            +"}"
        +"}");
    }
    /**
     * Enviamos un mensaje para que sea imprimido en la pestaña de experts.
     * @param msj 
     */
    public void log(Object msj){
        StringBuffer temp = new StringBuffer();
        temp.append("{");
        temp.append("\"type\": \"log\",");
        //enviamos warning solo para que se vea como amarillo no por que sea un,
        //errror o algo.
        temp.append("\"label\": \"warning\",");
        temp.append("\"msj\":\""+msj+" \"");
        temp.append("}");
        this.writeNode(temp.toString());
    }
    /**
     * Enviamos un mensaje para que sea imprimido en la pestaña de jornual,
     * mensajes de aplicación en su mayoria.
     * @param msj 
     */
    public void msg(Object msj){
        StringBuffer temp = new StringBuffer();
        temp.append("{");
        temp.append("\"type\": \"journal\",");
        temp.append("\"label\": \"info\",");
        temp.append("\"msj\":\""+msj+" \"");
        temp.append("}");
        this.writeNode(temp.toString());
    }
    /**
     * Notificamos que entro una nueva orden.
     * @param orden 
     */
    public void nwOrden(Orden orden){
        this.writeNode("{"
            +"\"type\":\"onOrder\","
            +"\"data\":"
                +"{"        
                    +"\"id\":\""+orden.getGrafId()+"\","
                    +"\"ordid\":\""+orden.getId()+"\","
                    +"\"tipo\":\""+orden.getSide()+"\"," //tipo de operacion
                    +"\"lotes\":\""+orden.getLotes()+"\","
                    +"\"symbol\":\""+orden.getSymbol()+"\","
                    +"\"precio\":\""+orden.getOpenPrice()+"\","
                    +"\"sl\":\""+orden.getSl()+"\","
                    +"\"tp\":\""+orden.getTp()+"\""
                +"}"
        +"}");
    }
    /**
     * Escribimos mensajes a node.
     * @param msj 
     */
    private void writeNode(String msj) {
        try {
            this.outNode.writeUTF(msj + "\n");
            int random = new Random().nextInt(10);
            //Esperamos entre 1-15 milis para prevenir perdida de mensajes.
            Thread.sleep(random);
        } catch (IOException ex) {
            System.out.println(ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MsjStreaming.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void inHandler(String msj) {
        
        try {
            JSONObject root = (JSONObject) new JSONParser().parse("{" + msj);
            JSONObject json = (JSONObject) root.get("msj");
           
            switch ((String) json.get("type")) {
                
                case "getOrders":
                    Orden temp ;
                    for (int i = 0; i < OrderHandler.ordersArr.size(); i++) {
                        temp = OrderHandler.ordersArr.get(i);
                        if(temp.IsActiva()){
                            this.nwOrden(temp);
                        }
                    }
                    break;
                  
                        
            }
        }catch (ParseException ex) {
            System.out.println("Colapso!: " + msj);
        }
    }
}
