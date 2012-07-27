package oms.util;

import quickfix.FieldNotFound;

/**
 *
 * @author omar
 */
public class fixToJson  {

    public String parseOrder(quickfix.fix42.ExecutionReport msj, String entry) throws FieldNotFound{
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
            buffer.append("\"grafica\" :\""+entry+"\",");
            buffer.append("\"Account\" :\"" + msj.getAccount().getValue()+"\",");
            buffer.append("\"ExecID\" : \"" + msj.getExecID().getValue() + "\",");
            buffer.append("\"OrderID\" :\"" + msj.getClOrdID().getValue() + "\",");
            buffer.append("\"NoOrder\" :" + msj.getOrderID().getValue() + ",");
            //buffer.append("\"MAGICMA\" :" +this.MAGICMA+ ",");
            buffer.append("\"Size\" :" + msj.getOrderQty().getValue() + ",");
            buffer.append("\"Type\" :"+ msj.getSide().getValue() + ",");
            buffer.append("\"Symbol\" :\""+ msj.getSymbol().getValue() + "\" ,");
            buffer.append("\"Price\" :" + msj.getLastPx().getValue() + ",");
            //buffer.append("\"StopL\" :" + this.sl  + ",");
            //buffer.append("\"TakeP\" :" + this.tp + ",");
            buffer.append("\"Commision\":" + "10,  " );
            buffer.append("\"Status\":" + "1");
        buffer.append("}");
        
        return buffer.toString();
    }
}
