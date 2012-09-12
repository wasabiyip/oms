package oms.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import quickfix.FieldNotFound;

/**
 * Convertimos un mensaje de FIX en una cadena JSON.
 * @author omar
 */
public class fixToJson  {

    /**
     * Extraemos datos de un ExecutionReport (datos de una orden entrante) y se
     * convierte en JSON.
     * @param msj
     * @param entry
     * @return
     * @throws FieldNotFound 
     */
    public String parseOrder(quickfix.fix42.ExecutionReport msj, String id, int magic) {
        StringBuffer buffer = new StringBuffer("");
        try {
            buffer.append("{");
                buffer.append("\"hora\" :\""+msj.getTransactTime().getValue()+"\",");
                buffer.append("\"grafica\" :\""+id+"\",");
                buffer.append("\"Account\" :\"" + msj.getAccount().getValue()+"\",");
                buffer.append("\"ExecID\" : \"" + msj.getExecID().getValue() + "\",");
                buffer.append("\"OrderID\" :\"" + msj.getClOrdID().getValue() + "\",");
                buffer.append("\"NoOrder\" :" + msj.getOrderID().getValue() + ",");
                buffer.append("\"MAGICMA\" :" +magic+ ",");
                buffer.append("\"Size\" :" + msj.getOrderQty().getValue() + ",");
                buffer.append("\"Type\" :"+ msj.getSide().getValue() + ",");
                buffer.append("\"Symbol\" :\""+ msj.getSymbol().getValue() + "\" ,");
                buffer.append("\"Price\" :" + msj.getLastPx().getValue() + ",");
                buffer.append("\"Commision\":" + "10,  " );
                buffer.append("\"Status\":" + "1");
            buffer.append("}");
            
            
        } catch (FieldNotFound ex) {
            buffer.append("HORROR!");
            Logger.getLogger(fixToJson.class.getName()).log(Level.SEVERE, null, ex);
        }
        return buffer.toString();
    }
}
