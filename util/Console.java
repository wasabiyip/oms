package oms.util;

import oms.deliverer.MessageHandler;

/**
 *
 * @author omar
 */
public class Console {
    /**
     * Enviamos un mensaje para que sea imprimido en la pestaña de experts.
     * @param msj 
     */
    public static void log(Object msj){
        StringBuffer temp = new StringBuffer();
        temp.append("{");
        temp.append("\"type\": \"log\",");
        //enviamos warning solo para que se vea como amarillo no por que sea un,
        //errror o algo.
        temp.append("\"label\": \"warning\",");
        temp.append("\"msj\":\""+msj+" \"");
        temp.append("}");
        MessageHandler.writeNode(temp.toString());
    }
    /**
     * Enviamos un mensaje para que sea imprimido en la pestaña de jornual,
     * mensajes de aplicación en su mayoria.
     * @param msj 
     */
    public static void msg(Object msj){
        StringBuffer temp = new StringBuffer();
        temp.append("{");
        temp.append("\"type\": \"journal\",");
        temp.append("\"label\": \"info\",");
        temp.append("\"msj\":\""+msj+" \"");
        temp.append("}");
        MessageHandler.writeNode(temp.toString());
    }
}
