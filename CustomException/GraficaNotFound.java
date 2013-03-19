package oms.CustomException;

/**
 *
 * @author omar
 */
public class GraficaNotFound extends Exception{
    public GraficaNotFound(String id){
        super("El horror: No pude notificar a la grafica" + id +" que su orden fuè cerrada." );
    }
    
}
