package oms.CustomException;

/**
 *
 * @author omar
 */
public class IndicatorLengthGap extends Exception{
    public IndicatorLengthGap(String symbol, Integer length){
        super(" Al llenar el indicador de "+symbol +" - "+length + " no sé que hacer...");
    }
}
