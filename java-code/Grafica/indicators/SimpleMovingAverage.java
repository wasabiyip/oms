package oms.Grafica.indicators;

import java.util.ArrayList;

/**
 * Utility class to calculate simple moving average given a set of values and a number of 
 * values to calculate it on.
 * */
public class SimpleMovingAverage {
	private int n=0;
	
	/**
	 * mean field is the mean for the given set of N values
	 */
	private double mean;
	
	/**
	 * Collection that includes the values to calculate the SMA
	 */
	private ArrayList<Double> values = new ArrayList();

	/**
	 * Gets the offset attribute used to get an old SMA given a list of chronological values.
	 * 
	 * @return the offset
	 * */
	private int offset=0;
	
	/**
	 * Constructor to create a SimpleMovingAverage object
	 */
	public SimpleMovingAverage() {
		setN(10);
	}
	/**
	 * Constructor to create a SimpleMovingAverage object
	 */
	public SimpleMovingAverage(int n, ArrayList<Double> values) {
		setN(n);
                //System.out.println(values.size());
		this.values.addAll(values);
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
	 * Method that calculates the SMA based on the values and the ('n') number of values 
	 */
	public double getSMA() {
		double mean=0;
		int size=values.size();
		if (values!=null && size>=getN()){
			int cnt=0;
			for (int i=size-getN();i<size;i++){
				mean+=values.get(i);
				cnt++;
			}
			assert(cnt==getN());
			mean=mean/getN();
			setMean(mean);
		}
		return mean;
	}
	/**
	 * @param mean the mean to set
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}
	/**
	 * @return the mean
	 */
	public double getMean() {
		return mean;
	}
	
	/**
	 * Gets the offset attribute used to get an old simple moving average given a list of chronological values.
	 * 
	 * @return the offset
	 * */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * Sets the offset used to calculate an old simple moving average given a list of chronological values.
	 * 
	 * @param offset the offset value to set
	 * */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
	
