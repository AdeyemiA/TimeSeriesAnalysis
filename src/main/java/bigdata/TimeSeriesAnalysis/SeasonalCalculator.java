/**
 * 
 */
package bigdata.TimeSeriesAnalysis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author adeyemi
 *
 */
public class SeasonalCalculator {
	private static String autumn;
	private static String spring;
	private static String summer;
	private static String winter;
	
	public SeasonalCalculator() {
		autumn = ServerConfiguration.getConfiguration(autumn);
		spring = ServerConfiguration.getConfiguration(spring);
		summer = ServerConfiguration.getConfiguration(summer);
		winter = ServerConfiguration.getConfiguration(winter);
	}
	
	public Map<String, List> aggregateSeasons(Map<String, List> yearMap) {
		Map<String, List> seasonalMap = new ConcurrentHashMap<String, List>();
		
		return seasonalMap;
	}

}
