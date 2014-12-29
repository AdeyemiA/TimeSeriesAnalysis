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
	protected static final int numOfSeasons = Integer.parseInt(ServerConfiguration.getConfiguration("seasons"));

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
		
		// n indicates the number of seasons accumulated from the first season of the input file
		int n = 0;
		List<List> seasonalValues = new ArrayList<List>();
		List<String> seasonalKeys = new ArrayList<String>();
		Integer[] outerKeys = trendMap.keySet().toArray(new Integer[trendMap.keySet().size()]);
		Arrays.sort(outerKeys);
		
		for(int i = 0; i < outerKeys.length; ++i) {
			//n += trendMap.get(outerKeys[i]).keySet().size();
			Set<String> innerKeySet = trendMap.get(outerKeys[i]).keySet();
			String[] innerKeys = innerKeySet.toArray(new String[innerKeySet.size()]);
			Arrays.sort(innerKeys);
			n += innerKeys.length;
			
			// populate the seasonalValues with the lists of the values for each year, and each season per year
			Map<String, List> innerMap = trendMap.get(outerKeys[i]);
			for(int j = 0; j < innerKeys.length; ++j) {
				seasonalValues.add(innerMap.get(innerKeys[j]));
				seasonalKeys.add(outerKeys[i] + "=" + innerKeys[j]);
			}
		}
		
		// t is the incremental time quantity starting from t = 1
		// y is the variable of interest to predict which is at the seasonalValues.get(k).get(variable) position
		Vector<Integer> t = new Vector<Integer>(n);
		Vector<Float> y = new Vector<Float>(n);
		for(int k=0; k < seasonalValues.size(); ++k) {
			y.add((Float) seasonalValues.get(k).get(variable));
			t.add(k + 1);
		}
		
		// b1 is the slope and b0 is the intercept of the trend line
		Float b1 = newPredictor.getSlope(t, y, n);
		Float b0 = newPredictor.getIntercept(t, y, b1);
		//System.out.println("Slope is " + b1 + " and intercept is " + b0 );
		
		// computing the trend line Y_hat = b0 + b1*t
		Vector<Float> y_hat = new Vector<Float>(n);
		for(int l=0; l < t.size(); ++l) {
			y_hat.add(b0 + (b1 * (Integer) t.get(l)));
		}
		
		// compute the Seasonal and Interference quantity (SI) from the variable and trend (Y/Y_hat = SI)
		Vector<Float> s_i = new Vector<Float>(n);
		for(int m=0; m < y_hat.size(); ++m) {
			s_i.add(y.get(m)/y_hat.get(m));
		}
		
		// the seasonal value only after the effect of I is averaged away
		Vector<Float> s = new Vector<Float>(numOfSeasons);
		
		// check all seasons and average the values to compute avg(Y/Y_hat = SI) to remove I effects
		// seasonalKeys and seasonalValues (s_i as per extracted predicted variable from inner list) are the same length 
		// the season for each year is separated by an "=" with the year in the first position .ie.e year=seasoni
		for(int i = 1; i <= numOfSeasons; ++i) {
			float cumulative = 0.0f;
			int entries = 0;
			String[] splitYearSeason = null;
			for(int j = 0; j < seasonalKeys.size(); ++j) {
				splitYearSeason = seasonalKeys.get(j).split("=");
				
				// if the seasoni is found, get the positional value from s_i and add together
				if(splitYearSeason[1].equals("season" + i)) {
					cumulative = cumulative + s_i.get(j);
					++entries;
				}
			}
			s.add(cumulative/entries);
		}
		//System.out.println("s_i content is " + s_i.toString() + " and contents of s is " + s.toString());
		// forecast for the num years
		Vector<Float> predictedValues = new Vector<Float>(num * numOfSeasons);
		//System.out.println("Size of predicted values is : " + (num * numOfSeasons));
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
