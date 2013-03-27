package oms.CustomException;

/**
 * @author omar
 * Si al buscar una Orden en el array en donde se almacenan estoas, no se encuentra
 * lanzamos este tipo de excepción.
 */
public class OrdenNotFound extends Exception{
    
    public OrdenNotFound(String id){
        super(" No se pudo encontrar una orden para "+ id +", esta orden no existe! ");
    }
}
