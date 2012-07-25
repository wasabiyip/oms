package oms.deliverer;

import java.util.logging.Level;
import java.util.logging.Logger;
import quickfix.Session;
import quickfix.SessionNotFound;
import quickfix.fix42.NewOrderSingle;
/**
 *
 * @author omar
 */
public class OrderHandler {
    /**
     * Metodo que envia las ordenes a Currenex, es sincronizado para que no se 
     * confunda si muchas graficas quieren enviar ordenés al mismo tiempo.
     * @param msj 
     */
    public synchronized static void sendOrder(Object msj){
        
        try {
            Session.sendToTarget((NewOrderSingle)msj, SenderApp.sessionID);    
        } catch (SessionNotFound ex) {
            Logger.getLogger(OrderHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
