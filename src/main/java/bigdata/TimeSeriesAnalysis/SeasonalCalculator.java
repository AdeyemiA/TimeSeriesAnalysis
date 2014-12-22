/**
 * 
 */
package bigdata.TimeSeriesAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adeyemi
 *
 */
public class SeasonalCalculator {
	protected static Logger log = LoggerFactory.getLogger(SeasonalCalculator.class);
	private static final String autumn = ServerConfiguration.getConfiguration("autumn");
	private static final String spring = ServerConfiguration.getConfiguration("spring");
	private static final String summer = ServerConfiguration.getConfiguration("summer");
	private static final String winter = ServerConfiguration.getConfiguration("winter");
	private static final int[] MONTHS_30 = new int[]{4, 6, 9, 11};
	
	public SeasonalCalculator() {
		//autumn = ServerConfiguration.getConfiguration("autumn");
		//spring = ServerConfiguration.getConfiguration("spring");
		//summer = ServerConfiguration.getConfiguration("summer");
		//winter = ServerConfiguration.getConfiguration("winter");
	}
	
	/**
	 * 
	 * @param dateRange - The Array of Date ranges each of syntax month/day to be split into int[]
	 * @return int[] - An array of int range, each element is month * 100 + day
	 * 				lower limit is int[0] and upper is int[1]
	 */
	private int[] getIntFromStringDate(String[] dateRange) {
		int[] intRange = new int[2];
		for(int i = 0; i < dateRange.length - 1; ++i) {
			String[] mnDay = dateRange[i].split("/");
			intRange[i] = getIntFromStringDate(dateRange[i]);
		}
		return intRange;
	}
	
	/**
	 * 
	 * @param date - The String of the form month/day to be split into int
	 * @return int - coalculated by month * 100 + day for comparison 
	 */
	private int getIntFromStringDate(String date) {
		String[] mnDay = date.split("/");
		return Integer.parseInt(mnDay[0]) * 100 + Integer.parseInt(mnDay[1]);		
	}
	
	public Map<String, List> aggregateSeasons(Map<Integer, Map<String, List>> yearMap) {
		// initialize the variables. Each key for seasonal would be the autumn,spring,.. values read from file
		Map<String, List> seasonalMap = new ConcurrentHashMap<String, List>();
		Set<Integer> yearsSet = yearMap.keySet();
		Integer[] yearsArray = (Integer[]) yearsSet.toArray();
		Arrays.sort(yearsArray, 0, yearsArray.length - 1);		
		// extract the months and days corresponding to a season
		String[] s_autumn = autumn.split("-");
		String[] s_spring = spring.split("-");
		String[] s_summer = summer.split("-");
		String[] s_winter = winter.split("-");
		
		int[] l_autumn = getIntFromStringDate(s_autumn);
		int[] l_spring = getIntFromStringDate(s_spring);
		int[] l_summer = getIntFromStringDate(s_summer);
		int[] l_winter = getIntFromStringDate(s_winter);
		String season;
		int year;
		int seasonEntries = 0;
		Map<String, List> monthlyMap;
		List<Float> cumulative = new ArrayList<Float>();
		
		// get each year value for each year key
		for(int index = 0; index < yearsArray.length -1; ++index) {
			season = null;
			year = yearsArray[index];
			monthlyMap = yearMap.get((Integer) year);
			Set<String> monthSet = monthlyMap.keySet();
			String[] monthArray = (String[]) monthSet.toArray();
			Arrays.sort(monthArray, 0, monthArray.length - 1);
			int monthDay;
			
			// get each daily value for each date key
			for(int idx = 0; idx < monthArray.length - 1; ++idx) {
				monthDay = getIntFromStringDate(monthArray[idx]);
				if((monthDay >= l_autumn[0]) && (monthDay <= l_autumn[1])) {
					// date is in the autumn season
					if(season != null && season.equals(autumn)) {
						// get the list for this key and add to accumulated value
						// increase the entries count
						cumulative = sumListOfFloats(cumulative, monthlyMap.get(monthArray[idx]));
						++seasonEntries;
					}else if(season != null) {
						// last entry was a different season, average and initialize
						List<Float> avgList = averageListOfFloats(cumulative, seasonEntries);
						seasonalMap.put(season, avgList);
						log.info("Averaging for season " + season + " and year " + year + ". ");
						season = autumn;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}else if(season == null) {
						// we are jut starting out with the iteration
						season = autumn;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}
					
				}else if((monthDay >= l_spring[0]) && (monthDay <= l_spring[1])) {
					// date is in the spring season
					
				}else if((monthDay >= l_summer[0]) && (monthDay <= l_summer[1])) {
					// date is in the summer season
					
				}else if((monthDay >= l_winter[0]) && (monthDay <= l_winter[1])){
					// date is in the winter season
					
				}else {
					log.error("The configuration properties for the seasons are invalid");
					log.error("Ensure you set the dates properly");
				}
				
			}
		}
		return seasonalMap;
	}
	
	/**
	 * 
	 * @param cumulative - Accumulator to store list of floats
	 * @param entry - The List<Float> which is added to the cumulative List<Float>
	 * @return cumulative - Return the accumulator List<Float> after summing entry
	 */
	private List<Float> sumListOfFloats(List<Float> cumulative, List<Float> entry) {
		for(int k = 0; k < cumulative.size(); ++k) {
			cumulative.add(k, cumulative.get(k) + entry.get(k));
		}
		return cumulative;
	}

	/**
	 * 
	 * @param cumulative - Accumulator to store the averaged Floats
	 * @param numOfIterations - The number of Iterations to average over
	 * @return cumulative - Return the accumulator List after dividing each element
	 */
	private List<Float> averageListOfFloats(List<Float> cumulative, int numOfIterations) {
		for(int l = 0; l < cumulative.size(); ++l) {
			cumulative.add(l, cumulative.get(l)/numOfIterations);
		}
		return cumulative;
	}
	
	public List<Float> computeRegularSeason(Map<String, List> seasons) {
		return array_of_list;
	}
}
