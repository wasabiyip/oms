package oms.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import oms.deliverer.Orden;
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
    public String parseOrder(Orden orden) {
        StringBuffer buffer = new StringBuffer("");
       
        buffer.append("{");
            buffer.append("\"horaOpen\" :\""+ orden.getHoraOpen()+"\",");
            buffer.append("\"grafica\" :\""+orden.getGrafId()+"\",");
            buffer.append("\"account\" :\"" + orden.getAccount()+"\",");
            buffer.append("\"execID\" : \"" + orden.getExecId()+ "\",");
            buffer.append("\"OrderID\" :\"" + orden.getId()+ "\",");
            buffer.append("\"noOrder\" :" + orden.getBrokerOrdId() + ",");
            buffer.append("\"MAGICMA\" :" +orden.getMagic()+ ",");
            buffer.append("\"size\" :" + orden.getLotes() + ",");
            buffer.append("\"type\" :"+ orden.getSide() + ",");
            buffer.append("\"symbol\" :\""+ orden.getSymbol()+ "\" ,");
            buffer.append("\"price\" :" + orden.getOpenPrice() + ",");
            buffer.append("\"cPrice\" :" + orden.getClosePrice()+ ",");
            buffer.append("\"sl\" :" + orden.getSl()+",");
            buffer.append("\"tp\" :" + orden.getTp()+",");
            buffer.append("\"horaClose\" : \"" + orden.getHoraClose()+ "\",");
            buffer.append("\"razon\" : \"" + orden.getReason()+ "\",");
            buffer.append("\"comision\":" + "10,  " );
        buffer.append("}");
        return buffer.toString();
    }
}
