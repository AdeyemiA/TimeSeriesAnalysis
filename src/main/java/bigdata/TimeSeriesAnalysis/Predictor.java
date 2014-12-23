/**
 * 
 */
package bigdata.TimeSeriesAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * @author adeyemi
 *
 */
public class Predictor {
	protected int numOfYears = 0;
	protected static Map<Integer, Map<String, List>> trendMap;
	protected static int numOfSeasons = 4;

	// constructor passed the number of years to predict
	public Predictor(int years, Map<Integer, Map<String, List>> trendMap) {
		this.numOfYears = years;
		this.trendMap = trendMap;
	}
	
	/**
	 * 
	 * @param num - Number of years to predict values for
	 * @param variable - The positional variable to predict (checked for validity before call)
	 * @param trend - The map that holds the values of the variables to predict
	 */
	public static Vector<Float> predict(int num, int variable, Map<Integer, Map<String, List>> trend) {
		Predictor newPredictor = new Predictor(num, trend);
		int n = 0;
		List<List> seasonalValues = new ArrayList<List>();
		Integer[] outerKeys = trendMap.keySet().toArray(new Integer[trendMap.keySet().size()]);
		Arrays.sort(outerKeys);
		
		for(int i = 0; i < outerKeys.length; ++i) {
			//n += trendMap.get(outerKeys[i]).keySet().size();
			Set<String> innerKeySet = trendMap.get(outerKeys[i]).keySet();
			String[] innerKeys = innerKeySet.toArray(new String[innerKeySet.size()]);
			Arrays.sort(innerKeys);
			n += innerKeys.length;
			
			Map<String, List> innerMap = trendMap.get(outerKeys[i]);
			for(int j = 0; j < innerKeys.length; ++j) {
				seasonalValues.add(innerMap.get(innerKeys[j]));
			}
		}
		
		Vector<Integer> t = new Vector<Integer>(n);
		Vector<Float> y = new Vector<Float>(n);
		for(int k=0; k < seasonalValues.size(); ++k) {
			y.add((Float) seasonalValues.get(k).get(variable));
			t.add(k + 1);
		}
		
		Float b1 = newPredictor.getSlope(t, y, n);
		Float b0 = newPredictor.getIntercept(t, y, b1);
		
		// computing the trend line Y_hat = b0 + b1*t
		Vector<Float> y_hat = new Vector<Float>(n);
		for(int l=0; l < t.size(); ++l) {
			y_hat.add(b0 + (b1 * (Integer) t.get(l)));
		}
		
		// compute the Seasonal and Interference quantity from the variable and trend (Y/Y_hat)
		Vector<Float> s_i = new Vector<Float>(n);
		for(int m=0; m < y_hat.size(); ++m) {
			s_i.add(y.get(m)/y_hat.get(m));
		}
		
		Vector<Float> s = new Vector<Float>(numOfSeasons);
		
		//for(int i = 0; i < s.size(); ++i) {
			//if(i == 0)
			//Float tmp = s_i.get(0) + s_i.get(1) + s_i.get(numOfSeasons * (i+1)) + s_i.get(numOfSeasons * numOfSeasons * (i+1)) + ;
			
		//}
		s.add((s_i.get(0) + s_i.get(1) + s_i.get(5) + s_i.get(9) + s_i.get(13))/5);
		s.add((s_i.get(2) + s_i.get(6) + s_i.get(10) + s_i.get(14))/4);
		s.add((s_i.get(3) + s_i.get(7) + s_i.get(11) + s_i.get(15))/4);
		s.add((s_i.get(4) + s_i.get(8) + s_i.get(12) + s_i.get(16))/4);
		
		// forecast for the num years
		Vector<Float> predictedValues = new Vector<Float>(num * numOfSeasons);
		for(int i = 0; i < num; ++i){
			for(int j = 0; j < s.size(); ++j) {
				Float tmp = newPredictor.getTrend(b0, b1, ++n);
				predictedValues.add(tmp * s.get(j));
			}
		}
		return predictedValues;
	}
	
	/**
	 * @param t - the time vector incremented over time
	 * @param y - the variable of interest to predict
	 * @param n - the total number of entries in training data
	 * @return - return the slope for the given dataset
	 */
	private Float getSlope(Vector t, Vector y, int n) {
		Vector<Integer> t_sqr = new Vector<Integer>(t.size());
		Vector<Float> ty = new Vector<Float>(t.size());
		Float sumy = 0.0f;
		Float sumt = 0.0f;
		Float sumty = 0.0f;
		Float sumt_sqr = 0.0f;
		for(int i=0; i < t.size(); ++i) {
			t_sqr.add( (Integer) t.get(i) * (Integer) t.get(i));
			ty.add((Integer) t.get(i) * (Float) y.get(i));
			sumy = sumy + (Float) y.get(i);
			sumt = sumt + (Integer) t.get(i);
			sumty = sumty + (Float) ty.get(i);
			sumt_sqr = sumt_sqr + (Integer) t_sqr.get(i);
		}
		Float b1 = 0.0f;
		b1 = (n*sumty - sumt*sumy)/(n*sumt_sqr - (sumt * sumt));
		return b1;
	}
	
	/**
	 * @param t - the time vector incremented over time
	 * @param y - the variable of interest to predict
	 * @param slope - the value b1, slope of the trend line
	 * @return - return the intercept for the given dataset
	 */
	private Float getIntercept(Vector t, Vector y, Float slope) {
		Float b0 = 0.0f;
		Float sumy = 0.0f;
		Float sumt = 0.0f;
		for(int i = 0; i < t.size(); ++i) {
			sumy += (Float) y.get(i);
			sumt += (Integer) t.get(i);
		}
		sumy = sumy/ (float) y.size();
		sumt = sumt/ (float) t.size();
		b0 = sumy - (slope * sumt);
		return b0;
	}
	
	private Float getTrend(Float b0, Float b1, int time) {
		return b0 + (b1 * time);
	}
}
