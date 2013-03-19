package oms.CustomException;

/**
 *
 * @author omar
 */
public class IndicatorLengthGap extends Exception{
    public IndicatorLengthGap(String symbol, Integer length){
        super("El horror: Al llenar el indicador de "+symbol +" - "+length + " no sé que hacer...");
    }
}
