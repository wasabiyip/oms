
package oms.util;

import oms.util.GMTDate;

/**
 * Solo imprimimos valores através de esta clase, que es un implementacion de 
 * método System.out.println, manejamos mensajes [success][error][warning]
 * [info] que se imprimen en la consola y se envian para los cliente.
 * @author omar
 */
public class Console {
    /**
     * Mensajes de algo realizado exitosamente
     * @param msj 
     */
    public synchronized static void success(Object msj){
        System.out.println("[SUCCESS] <"+GMTDate.getDate()+">| "+ msj);
    }
    /**
     * Mesansajes Informativos
     * @param msj 
     */
    public synchronized static void info(Object msj){
        System.out.println("[INFO] <"+GMTDate.getDate()+">| "+ msj);
    }
    /**
     * Mensajes de errores.
     * @param msj 
     */
    public synchronized static void error(Object msj){
        System.out.println("[ERROR] <"+GMTDate.getDate()+">| "+ msj);
    }
    /**
     * Mensajes de excepciones.
     * @param msj 
     */
    public synchronized static void exception(Object msj){
        System.out.println("[ERROR] <"+GMTDate.getDate()+">| "+ msj);
    }
    /**
     * Mensajes de error precautorio.
     * @param msj 
     */
    public synchronized static void warning(Object msj){
        System.out.println("[WARNING] <"+GMTDate.getDate()+">| "+ msj);
    }
}
