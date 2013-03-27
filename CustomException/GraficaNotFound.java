package oms.CustomException;

/**
 *
 * @author omar
 */
public class GraficaNotFound extends Exception{
    public GraficaNotFound(String id){
        super("No pude notificar a la grafica" + id);
    }
    
}
