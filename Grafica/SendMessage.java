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
    public void nwOrden(ExecutionReport report,double sl, double tp){
        try {
            this.writeNode("{"
                +"\"type\":\"onOrder\","
                +"\"data\":"
                    +"{"        
                        +"\"id\":\""+this.stateFeed.getId()+"\","
                        +"\"ordid\":\""+report.getClOrdID().getValue()+"\","
                        +"\"tipo\":\""+report.getSide().getValue()+"\"," //tipo de operacion
                        +"\"lotes\":\""+(report.getOrderQty().getValue()/100000)+"\","
                        +"\"symbol\":\""+report.getSymbol().getValue()+"\","
                        +"\"precio\":\""+report.getAvgPx().getValue()+"\","
                        +"\"sl\":\""+sl+"\","
                        +"\"tp\":\""+tp+"\""
                    +"}"
            +"}");
        } catch (FieldNotFound ex) {
            Logger.getLogger(SendMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void clOrden(String id){
        
        this.writeNode("{"        
            +"\"type\":\"onOrderClose\","
            +"\"data\":"
            +"{"
                +"\"id\":\""+id+"\""
            +"}"
        +"}");
    }
    public void modOrden(String ordid, Double precio){
        
            this.writeNode("{"
                +"\"type\":\"orderModify\","
                +"\"data\":"
                +"{"
                    +"\"id\":\""+ordid+"\","
                    +"\"nwTp\":\""+precio+"\""
                +"}"
            +"}");
    }
    /**
     * Enviamos ordenes actuales de la grafica.
     */
    public void ordersInit(ArrayList<DBObject> data, String id){
        
         for(int i=0; i<data.size();i++){
            this.writeNode("{"
                +"\"type\":\"onOrderInit\","
                +"\"data\":"
                +"{"
                    +"\"id\":\""+id+"\","
                        +"\"ordid\":\""+data.get(i).get("OrderID") +"\","
                        +"\"tipo\":\""+data.get(i).get("Type")+"\"," //tipo de operacion
                        +"\"lotes\":\""+((Double)data.get(i).get("Size")/100000)+"\","
                        +"\"symbol\":\""+data.get(i).get("Symbol")+"\","
                        +"\"precio\":\""+data.get(i).get("Price")+"\","
                        +"\"sl\":\""+data.get(i).get("StopL") +"\","
                        +"\"tp\":\""+data.get(i).get("TakeP")+"\""
                    +"}"
            +"}");
         }
    }
    /**
     * Método que envia mensajes a node.
     * @param msj
     */
    private  void writeNode(String msj) {
        try {
            
            int random = new Random().nextInt(15);
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
