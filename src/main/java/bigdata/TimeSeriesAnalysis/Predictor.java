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

	// constructor passed the number of years to predict
	public Predictor(int years, Map<Integer, Map<String, List>> trendMap) {
		this.numOfYears = years;
		this.trendMap = trendMap;
	}
	
	/**
	 * 
	 * @param num - Number of years to predict values for
	 * @param variable - The positional variable to predict (checked for validity before call)
	 */
	public static void predict(int num, int variable, Map<Integer, Map<String, List>> trend) {
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
		
		// compute the Seasonal and Interference quantity from the variable and trend
		Vector<Float> s_i = new Vector<Float>(n);
		for(int m=0; m < y_hat.size(); ++m) {
			s_i.add(y.get(m)/y_hat.get(m));
		}
	}
	
	/**
	 * @param t - the time vector incremented over time
	 * @param y - the variable of interest to predict
	 * @param n - the total number of entries in training data
	 * @return - return the slope for the given dataset
	 */
	private Float getSlope(Vector t, Vector y, int n) {
		Vector<Float> t_sqr = new Vector<Float>(t.size());
		Vector<Float> ty = new Vector<Float>(t.size());
		Float sumy = 0.0f;
		Float sumt = 0.0f;
		Float sumty = 0.0f;
		Float sumt_sqr = 0.0f;
		for(int i=0; i < t.size(); ++i) {
			t_sqr.add((Float) t.get(i) * (Float) t.get(i));
			ty.add((Float) t.get(i) * (Float) y.get(i));
			sumy = sumy + (Float) y.get(i);
			sumt = sumt + (Float) t.get(i);
			sumty = sumty + (Float) ty.get(i);
			sumt_sqr = sumt_sqr + (Float) t_sqr.get(i);
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
			sumt += (Float) t.get(i);
		}
		sumy = sumy/y.size();
		sumt = sumt/t.size();
		b0 = sumy - (slope * sumt);
		return b0;
	}
	
	private Float getTrend(Float b0, Float b1, int time) {
		return b0 + (b1 * time);
	}
}
