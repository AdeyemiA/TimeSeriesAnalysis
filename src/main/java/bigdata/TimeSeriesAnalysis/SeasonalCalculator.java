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
 * This class will compute the seasonal averaging of the energy consumption data
 * It depends on the configuration of the date ranges in the config.properties file
 *
 */
public class SeasonalCalculator {
	protected static Logger log = LoggerFactory.getLogger(SeasonalCalculator.class);
	private static final String autumn = ServerConfiguration.getConfiguration("autumn");
	private static final String spring = ServerConfiguration.getConfiguration("spring");
	private static final String summer = ServerConfiguration.getConfiguration("summer");
	private static final String winter = ServerConfiguration.getConfiguration("winter");
	private static final int[] MONTHS_30 = new int[]{4, 6, 9, 11};
	private static final Object m_Object = new Object();
	private static SeasonalCalculator seasonalCalculator = null;
	
	public SeasonalCalculator() {
		//autumn = ServerConfiguration.getConfiguration("autumn");
		//spring = ServerConfiguration.getConfiguration("spring");
		//summer = ServerConfiguration.getConfiguration("summer");
		//winter = ServerConfiguration.getConfiguration("winter");
	}
	
	/**
	 * 
	 * @return - an instance of the SeasonalCalculator
	 */
	public static SeasonalCalculator getInstance() {
		synchronized(m_Object) {
			if(seasonalCalculator == null) {
				seasonalCalculator = new SeasonalCalculator();
			}
		}
		return seasonalCalculator;
	}
	
	/**
	 * 
	 * @param dateRange - The Array of Date ranges each of syntax month/day to be split into int[]
	 * @return int[] - An array of int range, each element is month * 100 + day
	 * 				lower limit is int[0] and upper is int[1]
	 */
	private int[] getIntFromStringDate(String[] dateRange) {
		int[] intRange = new int[2];
		for(int i = 0; i < dateRange.length; ++i) {
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
	
	/**
	 * 
	 * @param yearMap - The mapping of the yearly energy consumption, with the values a map of the daily entries
	 * @return - A yearly energy consumption map, with the values being a map of the seasonal (autumn, winter..) entries
	 */
	public static Map<Integer, Map<String, List>> aggregateSeasons(Map<Integer, Map<String, List>> yearMap) {
		// get an instance of the calculator
		SeasonalCalculator seasonalCalculator = getInstance();
		
		// initialize the variables. Each key for seasonal would be the autumn,spring,.. values read from file
		Map<Integer, Map<String, List>> annualSeasonalMap = new ConcurrentHashMap<Integer, Map<String, List>>();
		Map<String, List> seasonalMap = new ConcurrentHashMap<String, List>();
		Set<Integer> yearsSet = yearMap.keySet();
		Integer[] yearsArray = yearsSet.toArray(new Integer[yearsSet.size()]);
		Arrays.sort(yearsArray, 0, yearsArray.length);
		
		// extract the months and days corresponding to a season as a string array
		String[] s_autumn = autumn.split("-");
		String[] s_spring = spring.split("-");
		String[] s_summer = summer.split("-");
		String[] s_winter = winter.split("-");
		
		// convert the elements of the string array into int values for comparison
		int[] l_autumn = seasonalCalculator.getIntFromStringDate(s_autumn);
		int[] l_spring = seasonalCalculator.getIntFromStringDate(s_spring);
		int[] l_summer = seasonalCalculator.getIntFromStringDate(s_summer);
		int[] l_winter = seasonalCalculator.getIntFromStringDate(s_winter);
		
		System.out.println(l_autumn[0] + " " + l_autumn[1]);
		//initialize variables
		String season;
		int year;
		int seasonEntries = 0;
		Map<String, List> monthlyMap;
		List<Float> cumulative = new ArrayList<Float>();
		
		// get each year value for each year key
		for(int index = 0; index < yearsArray.length; ++index) {
			season = null;
			year = yearsArray[index];
			monthlyMap = yearMap.get(year);
			Set<String> monthSet = monthlyMap.keySet();
			System.out.println("There are " + monthSet.size() + " set elements before sort for year " + year);
			String[] monthArray = monthSet.toArray(new String[monthSet.size()]);
			Arrays.sort(monthArray, 0, monthArray.length);
			int monthDay;
			System.out.println("There are " + monthArray.length + " inner keys for " + year + " year");
			// get each daily value for each date key
			for(int idx = 0; idx < monthArray.length; ++idx) {
				monthDay = seasonalCalculator.getIntFromStringDate(monthArray[idx]);
				System.out.println("Monthday is " + monthDay + " after casting to int");
				if((monthDay >= l_autumn[0]) && (monthDay <= l_autumn[1])) {
					
					// date is in the autumn season
					if(season != null && season.equals(autumn)) {
						
						// get the list for this key and add to accumulated value
						// increase the entries count
						cumulative = seasonalCalculator.sumListOfFloats(cumulative, monthlyMap.get(monthArray[idx]));
						++seasonEntries;
					}else if(season != null) {
						
						// last entry was a different season, average and initialize
						List<Float> avgList = seasonalCalculator.averageListOfFloats(cumulative, seasonEntries);
						seasonalMap.put(season, avgList);
						System.out.println("Averaging for season " + season + " and year " + year + ". ");
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
					if(season != null && season.equals(spring)) {
						
						// get the list for this key and add to accumulated value
						// increase the entries count
						cumulative = seasonalCalculator.sumListOfFloats(cumulative, monthlyMap.get(monthArray[idx]));
						++seasonEntries;
					}else if(season != null) {
						
						// last entry was a different season, average and initialize
						List<Float> avgList = seasonalCalculator.averageListOfFloats(cumulative, seasonEntries);
						seasonalMap.put(season, avgList);
						log.info("Averaging for season " + season + " and year " + year + ". ");
						season = spring;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}else if(season == null) {
						
						// we are jut starting out with the iteration
						season = spring;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}
				}else if((monthDay >= l_summer[0]) && (monthDay <= l_summer[1])) {
					
					// date is in the summer season
					if(season != null && season.equals(summer)) {
						
						// get the list for this key and add to accumulated value
						// increase the entries count
						cumulative = seasonalCalculator.sumListOfFloats(cumulative, monthlyMap.get(monthArray[idx]));
						++seasonEntries;
					}else if(season != null) {
						
						// last entry was a different season, average and initialize
						List<Float> avgList = seasonalCalculator.averageListOfFloats(cumulative, seasonEntries);
						seasonalMap.put(season, avgList);
						log.info("Averaging for season " + season + " and year " + year + ". ");
						season = summer;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}else if(season == null) {
						
						// we are jut starting out with the iteration
						season = summer;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}
				}else if((monthDay >= l_winter[0]) && (monthDay <= l_winter[1])){
					
					// date is in the winter season
					if(season != null && season.equals(winter)) {
						
						// get the list for this key and add to accumulated value
						// increase the entries count
						cumulative = seasonalCalculator.sumListOfFloats(cumulative, monthlyMap.get(monthArray[idx]));
						++seasonEntries;
					}else if(season != null) {
						
						// last entry was a different season, average and initialize
						List<Float> avgList = seasonalCalculator.averageListOfFloats(cumulative, seasonEntries);
						seasonalMap.put(season, avgList);
						log.info("Averaging for season " + season + " and year " + year + ". ");
						season = winter;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}else if(season == null) {
						
						// we are jut starting out with the iteration
						season = winter;
						cumulative = monthlyMap.get(monthArray[idx]);
						seasonEntries = 1;
					}
					
				}else {
					log.error("The configuration properties for the seasons are invalid");
					log.error("Ensure you set the dates properly");
				}
				
			}
			annualSeasonalMap.put(year, seasonalMap);
		}
		return annualSeasonalMap;
	}
	
	/**
	 * 
	 * @param cumulative - Accumulator to store list of floats
	 * @param entry - The List<Float> which is added to the cumulative List<Float>
	 * @return cumulative - Return the accumulator List<Float> after summing entry
	 */
	private List<Float> sumListOfFloats(List<Float> cumulative, List<Float> entry) {
		List<Float> temp = new ArrayList<Float>();
		for(int k = 0; k < cumulative.size(); ++k) {
			temp.add(k, cumulative.get(k) + entry.get(k));
		}
		return temp;
	}

	/**
	 * 
	 * @param cumulative - Accumulator to store the averaged Floats
	 * @param numOfIterations - The number of Iterations to average over
	 * @return cumulative - Return the accumulator List after dividing each element
	 */
	private List<Float> averageListOfFloats(List<Float> cumulative, int numOfIterations) {
		List<Float> temp = new ArrayList<Float>();
		for(int l = 0; l < cumulative.size(); ++l) {
			temp.add(l, cumulative.get(l)/numOfIterations);
		}
		return temp;
	}
	
/*	public List<Float> computeRegularSeason(Map<String, List> seasons) {
		return array_of_list;
	}*/
}
