package oms.Grafica.indicators;

import java.util.ArrayList;

public class StandardDeviation {

	/**
	 * values field holds the values to be used to determine the standard deviation
	 */
	private ArrayList<Double> values=new ArrayList<Double>();
	/**
	 * N field is the number of values to consider to calculate the standard deviation
	 */
	private int N;
	/**
	 * mean field is the mean for the given set of N values considered to calculate the standard deviation
	 */
	private double mean;
	
	/**
	 * Offset value used to calculate a standard deviation different than the one calculate with the last set of N values.
	 * */
	private int offset=0;
	
	public StandardDeviation(){
		setN(10);
	}
	
	public StandardDeviation(int N){
		setN(N);
	}

	public StandardDeviation(int N,ArrayList<Double> values){
		setN(N);
		this.values.addAll(values);
	}
	
	/**
	 * Method used to calculate the standard deviation.
	 * */
	public double calculateStdDev(){
		double stdDev=0;
		calculateMean();
		int size=values.size();
		if (size<(N+offset)){
			return stdDev;
		}
		int cnt=0;
		for (int i=size-(N+offset);i<(size-offset);i++){
			stdDev+=Math.pow((values.get(i)-mean),2);
			cnt++;
		}
		stdDev=Math.sqrt(stdDev/N);
		return stdDev;
	}
	
	/**
	 * Method used to calculate the mean of the last N given set of values.
	 * Used to calculate the mean in the @see calculateStdDev()
	 * */
	private double calculateMean(){
		double mean=0;
		int size=values.size();
		if (size<N){
			return mean;
		}
		int cnt=0;
		for (int i=size-N;i<size;i++){
			mean+=values.get(i);
			cnt++;
		}
		assert(cnt==N);
		mean=mean/N;
		setMean(mean);
		return mean;
	}
	
	/**
	 * Sets the mean attribute.
	 * 
	 * @param mean the values of the mean to be set in the mean attribute to this object.*/
	private void setMean(double mean) {
		this.mean=mean;
	}

	/**
	 * @param n the n to set
	 */
	public void setN(int n) {
		N = n;
	}
	/**
	 * @return the n
	 */
	public int getN() {
		return N;
	}

	/**
	 * Gets the offset attribute used to get an old standard deviation given a list of chronological values.
	 * 
	 * @return the offset
	 * */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset used to calculate an old standard deviation given a list of chronological values.
	 * 
	 * @param offset the offset value to set
	 * */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	
}

