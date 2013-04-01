
package oms.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms.deliverer.MessageHandler;

/**
 * Solo imprimimos valores através de esta clase, que es un implementacion de 
 * método System.out.println, manejamos mensajes [error][warning][info][exception]
 * que se imprimen en la consola y se envian para los cliente, además escribimos
 * en un archivo todos los mensajes de error, warning y excepciones, por día 
 * de trade.
 * @author omar
 */
public class Console {
    
    private static PrintWriter blackBox;
    /**
     * Añadimos path de blackBox. Lo tratamos como tipo constructor.
     */
    public static void setPath(String path){
        try {
            blackBox = new PrintWriter(path+"/OMS/log/app/"+GMTDate.getDate().getDate()+ ".log","UTF-8");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Mesansajes Informativos
     * @param msj 
     */
    public synchronized static void info(Object msj){
        String temp = "[INFO] <"+GMTDate.getDate()+">| "+ msj;
        System.out.println(temp);
        MessageHandler.mStreaming.msg(temp);
    }
    /**
     * Mensajes de errores.
     * @param msj 
     */
    public synchronized static void error(Object msj){
        String temp = "[ERROR] <"+GMTDate.getDate()+"> "+ msj;
        System.out.println(temp);
        writeBlackBox(temp);
        
    }
    /**
     * Mensajes de excepciones.
     * @param msj 
     */
    public synchronized static void exception(Object msj){
        String temp = "[exception] <"+GMTDate.getDate()+"> "+ msj;
        System.out.println(temp);
        writeBlackBox(temp);
    }
    /**
     * Mensajes de error precautorio.
     * @param msj 
     */
    public synchronized static void warning(Object msj){
        String temp = "[warning] <"+GMTDate.getDate()+"> "+ msj;
        MessageHandler.mStreaming.msg(msj);
        System.out.println(temp);
        writeBlackBox(temp);
    }
    /**
     * Mensajes de aplicacion.
     * @param msj 
     */
    public synchronized static void log(Object msj){
        String temp = "[INFO] <"+GMTDate.getDate()+">| "+ msj;
        System.out.println(temp);
        MessageHandler.mStreaming.log(msj);
    }
    /**
     * Escribimos mensaje en archivo.
     * @param msj 
     */
    private synchronized static void writeBlackBox(String msj){
        blackBox.println(msj);
        blackBox.flush();
    }
}
