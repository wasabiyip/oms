package oms.Grafica;

import com.mongodb.DBObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import quickfix.FieldNotFound;
import quickfix.fix42.ExecutionReport;

/**
 *
 * @author omar
 */
public class SendMessage {

    private DataOutputStream outNode;
    private StateFeed stateFeed;
    
    public SendMessage(DataOutputStream outNode, StateFeed stateFeed) {    
        this.outNode = outNode;
        this.stateFeed = stateFeed;
        
    }

    public void logIn() {
        
        this.writeNode("{\"type\": \"login\", "
                + "\"name\":\"CLIENT_TCP\", "
                + "\"symbol\":\"" + this.stateFeed.getSymbol() + "\","
                + this.stateFeed.getExpertInfo() + ","
                + this.stateFeed.getExpertState()
                + "}");

    }
    public void ExpertState(){
        
        this.writeNode("{"
            + "\"type\": \"expert-state\","
            + "\"id\":\"" + this.stateFeed.getId() + "\","
            + this.stateFeed.getExpertState()
            +"}");
    }
    /**
     * Cuando una vela enviamos el estado del expert.
     */
    public void Candle(){
        
        this.writeNode("{"
        + "\"type\": \"onCandle\","
        + this.stateFeed.getExpertState()
        +"}");
    }
    /**
     * Enviamos evento de Open.
     */
    public void Open(){
        
        this.writeNode("{"
        +"\"type\": \"onOpen\","
        +"\"precio\": " + this.stateFeed.getAvgOpen()
        + "}");
    }        
    /**
     * Método que envia mensajes a node.
     * @param msj
     */
    private  void writeNode(String msj) {
        try {
            
            int random = new Random().nextInt(30);
            //Esperamos entre 1-15 milis para prevenir perdida de mensajes.
            Thread.sleep(random);
            outNode.writeUTF(msj + "\n");
            
        } catch (IOException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
