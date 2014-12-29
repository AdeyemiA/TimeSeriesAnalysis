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
	protected static final String autumn = ServerConfiguration.getConfiguration("autumn");
	protected static final String spring = ServerConfiguration.getConfiguration("spring");
	protected static final String summer = ServerConfiguration.getConfiguration("summer");
	protected static final String winter = ServerConfiguration.getConfiguration("winter");
	protected static final int[] MONTHS_30 = new int[]{4, 6, 9, 11};
	private static final Object m_Object = new Object();
	private static Vector<String> seasonRange = new Vector<String>();
	private static SeasonalCalculator seasonalCalculator = null;
	private static final int configSeasons = Integer.parseInt(ServerConfiguration.getConfiguration("seasons"));
	public SeasonalCalculator() {
		//autumn = ServerConfiguration.getConfiguration("autumn");
		//spring = ServerConfiguration.getConfiguration("spring");
		//summer = ServerConfiguration.getConfiguration("summer");
		//winter = ServerConfiguration.getConfiguration("winter");
		if((Integer) configSeasons instanceof Integer) {
			for(int i = 1; i <= configSeasons; ++i) {
				if(ServerConfiguration.containsConfiguration("season" + i)) {
					seasonRange.add(ServerConfiguration.getConfiguration("season" + i));
				}else {
					log.error("The configuration for season" + i + " does not exist");
					log.error("Please set the configuration in the file /resources/config.properties");
					System.exit(1);
				}
			}
		}
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
	public static int[] getIntFromStringDate(String[] dateRange) {
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
	public static int getIntFromStringDate(String date) {
		String[] mnDay = date.split("/");
		return Integer.parseInt(mnDay[0]) * 100 + Integer.parseInt(mnDay[1]);		
	}
	
	/**
	 * 
	 * @param yearMap - The mapping of the yearly energy consumption, with the values a map of the daily entries
	 * @return - A yearly energy consumption map, with the values being a map of the seasonal (autumn, winter..) entries
	 */
	public static Map<Integer, Map<String, List>> aggregateSeasons(Map<Integer, Map<Integer, List>> yearMap) {
		// get an instance of the calculator
		SeasonalCalculator seasonalCalculator = getInstance();
		
		// initialize the variables. Each key for seasonal would be the autumn,spring,.. values read from file
		Map<Integer, Map<String, List>> annualSeasonalMap = new ConcurrentHashMap<Integer, Map<String, List>>();
		Map<String, List> seasonalMap;
		Integer[] yearsArray = yearMap.keySet().toArray(new Integer[yearMap.keySet().size()]);
		Arrays.sort(yearsArray, 0, yearsArray.length);
		
		// extract the date ranges per season as a string array
		List<String[]> seasonDateRanges = new ArrayList<String[]>();
		for(int i = 0; i < seasonRange.size(); ++i) {
			seasonDateRanges.add(seasonRange.get(i).split("-"));
		}
		
		// convert the elements of the string array into int values for comparison
		List<int[]> seasonDateRangesInt = new ArrayList<int[]>();
		for(int i = 0; i < seasonRange.size(); ++i) {
			seasonDateRangesInt.add(getIntFromStringDate(seasonDateRanges.get(i)));
		}
		
		//initialize variables
		String season;
		int seasonEntries;
		Map<Integer, List> monthlyMap;
		List<Float> cumulative = new ArrayList<Float>();
		
		// get each year = yearsArray[index] get the daily readings in the year
		for(int index = 0; index < yearsArray.length; ++index) {
			season = null;
			seasonEntries = 0;
			seasonalMap = new ConcurrentHashMap<String, List>();
			monthlyMap = yearMap.get(yearsArray[index]);
			Set<Integer> monthSet = monthlyMap.keySet();
			Integer[] monthArray = monthSet.toArray(new Integer[monthSet.size()]);
			Arrays.sort(monthArray, 0, monthArray.length);

			// get each daily value for each date key
			for(int idx = 0; idx < monthArray.length; ++idx) {
				for(int j = 0; j < seasonDateRangesInt.size(); ++j) {
					int[] season_j = seasonDateRangesInt.get(j);
					if((monthArray[idx] >= season_j[0]) && (monthArray[idx] <= season_j[1])) {
						
						// date is in the autumn season
						if(season != null && season.equals("season" + (j+1))) {
							
							// get the list for this key and add to accumulated value
							// increase the entries count
							cumulative = seasonalCalculator.sumListOfFloats(cumulative, monthlyMap.get(monthArray[idx]));
							++seasonEntries;
							if(idx == monthArray.length - 1) {
								List<Float> avgList = seasonalCalculator.averageListOfFloats(cumulative, seasonEntries);
								seasonalMap.put(season, avgList);
								//System.out.println("Averaging for season " + season + " and year " + yearsArray[index] + ". ");
								//System.out.println("Average is " + avgList.toString() + " and num of iter is " + seasonEntries);
								cumulative = null;
								season = null;
								seasonEntries = 0;
							}
						}else if(season != null) {
							
							// last entry was a different season, average and initialize
							List<Float> avgList = seasonalCalculator.averageListOfFloats(cumulative, seasonEntries);
							seasonalMap.put(season, avgList);
							//System.out.println("Averaging for season " + season + " and year " + yearsArray[index] + ". ");
							//System.out.println("Average is " + avgList.toString());
							season = "season" + (j+1);
							cumulative = monthlyMap.get(monthArray[idx]);
							seasonEntries = 1;
							avgList = null;
						}else if(season == null) {
							
							// we are jut starting out with the iteration
							season = "season" + (j+1);
							cumulative = monthlyMap.get(monthArray[idx]);
							seasonEntries = 1;
						}
						
					}
				}
			}
			annualSeasonalMap.put(yearsArray[index], seasonalMap);
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
