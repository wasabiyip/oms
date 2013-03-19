package oms.Grafica;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.deliverer.GraficaHandler;

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
     * Método que envia mensajes a node.
     * @param msj
     */
    private  void writeNode(String msj) {
        try {
            
            int random = new Random().nextInt(GraficaHandler.graficas.size());
            //Esperamos entre 1 y el número de graficas enmilis para prevenir 
            //mensajes traslapados.
            Thread.sleep(random);
            this.outNode.writeUTF(msj + "\n");
            this.outNode.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Graphic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
