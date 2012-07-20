
package oms.Grafica.indicators;


import oms.Grafica.DAO.MongoDao;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Ivan - Hermano de César
 *
 */
public class BollingerBands {

    /**
     * StandardDeviation object used to calculate the standard deviation used in
     * this BollingerBands object.
	 *
     */
    private StandardDeviation standardDeviation;
    /**
     * SimpleMovingAverage object used to calculate the SMA in this
     * BollingerBands object.
	 *
     */
    private SimpleMovingAverage simpleMovingAverage;
    /**
     * The value of the lower band.
	 *
     */
    private double lowerBand = 0;
    /**
     * The value of the middle band.
	 *
     */
    private double middleBand = 0;
    /**
     * The value of the upper band.
	 *
     */
    private double upperBand = 0;
    /**
     * Array que almacena precios.
     */
    public ArrayList<Double> values = new ArrayList();
    /**
     * Variable para verificar que se tiene la cantidad correcta de valores.
     */
    public boolean go = false;
    /**
     * For Bollinger bands: a middle band being an N-period simple moving
     * average an upper band at K times an N-period standard deviation above the
     * middle band a lower band at K times an N-period standard deviation below
     * the middle band
     *
     * This is the 'N' value
     */
    private int n;
    
    public BollingerBands(int n,ArrayList historial) {
        
        this.setN(n);
        this.values = historial;  
        simpleMovingAverage = new SimpleMovingAverage(getN(), this.values);
        standardDeviation = new StandardDeviation(getN(), this.values);
        // Calculates simple moving average (SMA)
        middleBand = Math.rint(simpleMovingAverage.getSMA() * 100000) / 100000;
        // Calculates the upper band by getting the previously calculated SMA
        upperBand = Math.rint((simpleMovingAverage.getMean() + (standardDeviation.calculateStdDev() * 2)) * 100000) / 100000;
        // Calculates the lower band by getting the previously calculated SMA
        lowerBand = Math.rint((simpleMovingAverage.getMean() - (standardDeviation.calculateStdDev() * 2)) * 100000) / 100000;
    }
    
    /**
     * Constructor de BollingerBands
     *
     * @param n el número de periodos a calcular.
     */
    public BollingerBands(int n) {
        this.setN(n);
    }

    /**
     * Recibe un precio y lo envia a el array de valores para ser calculado
     *
     * @param val Nuevo precio.
     */
    public void setPrice(double val) {

        this.refreshArray(val);
        simpleMovingAverage = new SimpleMovingAverage(getN(), this.values);
        standardDeviation = new StandardDeviation(getN(), this.values);
        // Calculates simple moving average (SMA)
        middleBand = Math.rint(simpleMovingAverage.getSMA() * 100000) / 100000;
        // Calculates the upper band by getting the previously calculated SMA
        upperBand = Math.rint((simpleMovingAverage.getMean() + (standardDeviation.calculateStdDev() * 2)) * 100000) / 100000;
        // Calculates the lower band by getting the previously calculated SMA
        lowerBand = Math.rint((simpleMovingAverage.getMean() - (standardDeviation.calculateStdDev() * 2)) * 100000) / 100000;
    }

    /**
     *
     * @return upperBand Banda de arriba.
     */
    public Double getUpperBand() {
        return this.upperBand;
    }

    /**
     *
     * @return middleBand regresa el SMA
     */
    public Double getMiddleBand() {
        return this.middleBand;

    }

    /**
     *
     * @return lowerBand regresa la banda de abajo.
     */
    public Double getLowerBand() {
        return this.lowerBand;
    }

    /**
     * @param n the n to set
     */
    public void setN(int n) {
        this.n = n;
    }

    /**
     * @return the n
     */
    public int getN() {
        return n;
    }

    /**
     * Funcion que refresca el array de valores, siempre tiene solamente la
     * cantidad de N valores.
     *
     * @param val
     * @return
     */
    private ArrayList<Double> refreshArray(double val) {
        
        this.values.remove(0);
        this.values.add(n - 1, val);
        return values;
    }
    /*
     * @Override public void onTick(ISubject o) {
     * this.refreshArray(Candle.Open(0)); if (go) {
     *
     * simpleMovingAverage = new indicators.SimpleMovingAverage(getN(),
     * this.values); standardDeviation = new StandardDeviation(getN(),
     * this.values); // Calculates simple moving average (SMA) middleBand =
     * Math.rint(simpleMovingAverage.getSMA() * 100000) / 100000; // Calculates
     * the upper band by getting the previously calculated SMA upperBand =
     * Math.rint((simpleMovingAverage.getMean() +
     * (standardDeviation.calculateStdDev() * 2)) * 100000) / 100000; //
     * Calculates the lower band by getting the previously calculated SMA
     * lowerBand = Math.rint((simpleMovingAverage.getMean() -
     * (standardDeviation.calculateStdDev() * 2)) * 100000) / 100000; }
    }
     */
    public int getSize(){
        return values.size();
    }
}
