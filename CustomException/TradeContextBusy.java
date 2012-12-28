package oms.CustomException;

/**
 * Excepción que alerta si dos o más gráficas de la misma moneda pretenden enviar una
 * orden al mismo tiempo.
 * @author omar
 */
public class TradeContextBusy extends Exception{
    public TradeContextBusy(){
        super("Lo siento, el trade context esta busy.");
    }
    public TradeContextBusy(String order, String symbol){
        super("Lo siento no pude enviar la orden " +order+" por que el trade "
                + "context esta busy para "+symbol +".");
    }
}
