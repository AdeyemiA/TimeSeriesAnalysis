package bigdata.TimeSeriesAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.BasicConfigurator;
import org.jfree.data.time.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

/**
 * Runs the Time Series Analysis Application
 *
 */
public class App extends TimeSeries 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static Logger log = LoggerFactory.getLogger(App.class);
	
	protected static Text textFile;
	
	protected static String NAME;
	
	//protected static ServerConfiguration serverConfiguration;
	
	protected Map<String, Map<String, Float[]>> textMap;
	
	protected static Map<String, Float[]> cleanedMap;
	
	protected static String FILENAME = "household_power_consumption.txt";
	
	protected static int FILE_INDEX = 0;
	
	protected static String FILE_BASE = "Out";
	
	// default directory if running local
	protected static String DATA_DIR = "/tmp/";
	
	protected static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	protected static final String PROPERTIES_FILE = "config.properties";
	// no-args constructor
	public App() {
		this("TimeSeries");
	}
	
	/**
	 * 
	 * @param projectName - Args-Constructor taking a String for the application name. Default is TimeSeries
	 */
	public App(String projectName) {
		super(projectName);
		App.NAME = projectName;
		this.textMap =  Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Float[]>>());
		this.cleanedMap = Collections.synchronizedMap(new LinkedHashMap<String, Float[]>());
	}
	
	public String getName() {
		return App.NAME;
	}
	
	/**
	 * 
	 * @param array - Check if the/an array value(s)/element is null
	 * @return - Return True if Empty, false otherwise
	 */
	public boolean isEmpty(String[] array) {
		for(int i=0; i < array.length - 1; ++i) {
			if(array[i] == null || "?".equals(array[i]) || array[i] == "")
					return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param num - Convert the Long value to Float in MB
	 * @return - return the alternative of Long in Float MBytes
	 */
	public Float getMB(Long num) {
		return (float) (num/ (1024.0*1024.0));
	}
	
	/**
	 * 
	 * @param cumulative- the Float[] functioning as an accumulator
	 * @param entry - Float[] entry to add to the accumulator cumulative. The date and time fields have been excluded in this method call
	 * @return - return the accumulator Float[] after summation
	 */
	public Float[] sumLists(Float[] cumulative, Float[] entry) {
		for(int i=0; i < entry.length; ++i) {
			cumulative[i] = cumulative[i] + entry[i]; 
		}
		return cumulative;
	}
	
	/**
	 * 
	 * @param cumulative - the Float[] functioning as an accumulator 
	 * @param entry - String[] entry to add to the accumulator cumulative. String is parsed to Float
	 * @return - return the accumulator Float[] after summation
	 */
	public Float[] sumLists(Float[] cumulative, String[] entry) {
		for(int i=0; i < cumulative.length; ++i) {
			cumulative[i] = cumulative[i] + Float.parseFloat(entry[i+Integer.parseInt(ServerConfiguration.getConfiguration("columns.to.skip"))]); 
		}
		return cumulative;
	}
	
	/**
	 * 
	 * @param cumulative - Float[] to average over entries number of iterations
	 * @param entries - number of iterations to average the cumulative over
	 * @return - return the cumulative after averaging over entries, the number of iterations
	 */
	public Float[] avgLists(Float[] cumulative, int entries) {
		for(int i=0; i < cumulative.length; ++i) {
			cumulative[i] = cumulative[i] / entries; 
		}
		return cumulative;
	}
	
	/**
	 * 
	 * @param accumulated - Generic Array to initialize for assured behaviour
	 * @return - Return the initialized array of type T
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T[] initArray(T[] accumulated) {
		for(int i=0; i < accumulated.length; ++i) {
			if(accumulated[i] instanceof String)
				accumulated[i] = (T) "";
			else if(accumulated[i] instanceof Integer)
				accumulated[i] = (T) new Integer(0);
			else if(accumulated[i] instanceof Float)
				accumulated[i] = (T) new Float(0.0f);
			else if(accumulated[i] instanceof Double)
				accumulated[i] = (T) new Double(0.0);
			else
				accumulated[i] = (T) new Object();
		}
		return accumulated;
	}
	
	public Float[] initArray(Float[] accumulated) {
		for(int i=0; i < accumulated.length; ++i) {
			accumulated[i] = new Float(0.0f);
		}
		return accumulated;
	}
	
	/**
	 * 
	 * @param key - Takes a string key and associates to the array accumulated
	 * @param accumulated - Array that is written to file with an association with the String key
	 * @throws IOException
	 */
	public <T> void writeToFile(String key, Float[] accumulated) throws IOException {
		BufferedWriter outWriter = null;
        try {
			outWriter = new BufferedWriter(new FileWriter(App.DATA_DIR + App.FILE_BASE + App.FILE_INDEX + ".csv", true));
			StringBuffer s;
				s = new StringBuffer();
				//String header = "Date,Global_active_power,Global_reactive_power,Voltage,Global_intensity,Sub_metering_1,Sub_metering_2,Sub_metering_3";
				//outWriter.write(header);
				//outWriter.write(System.getProperty("line.separator"));
				for(int i=0; i < accumulated.length; ++i) {
					
					s.append(accumulated[i]);
					if(i != accumulated.length - 1) {
						s.append(",");
					}
				}
					outWriter.write(key + "," + s.toString());
					outWriter.write(System.getProperty("line.separator"));		

        } catch (UnsupportedEncodingException e) {

			System.out.println("Ensure you can use this encoding");
			e.printStackTrace();
		} catch (FileNotFoundException e) {

			System.out.println("Ensure you have write access");
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			outWriter.flush();
			outWriter.close();
		}
	}
	
	/**
	 * 
	 * @param readings - Takes a generic array and writes the value to a File
	 * @param sameRead - checks if the read operation is within a given key or start of a new key
	 * @throws IOException
	 */
	public <T> void writeToFile(T[] readings, boolean sameRead) throws IOException {
		BufferedWriter outWriter = null;
        try {
			outWriter = new BufferedWriter(new FileWriter(App.DATA_DIR + "map.txt", true));
			StringBuffer s;
			if(sameRead) {
					s = new StringBuffer();
					for(int i=2; i < readings.length; ++i) { 
						s.append(readings[i] + ";");
					}
						outWriter.write("*****");
						outWriter.write(readings[1] + ";" + s.toString());
						outWriter.write(System.getProperty("line.separator"));
			}else {
				s = new StringBuffer();
				for(int i=1; i < readings.length; ++i) { 
					s.append(readings[i] + ";");
				}
					outWriter.write(readings[0] + ";" + s.toString());
					//outWriter.
					outWriter.write(System.getProperty("line.separator"));		
			}

        } catch (UnsupportedEncodingException e) {
			System.out.println("Ensure you can use this encoding");
			e.printStackTrace();
		} catch (FileNotFoundException e) {

			System.out.println("Ensure you have write access");
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			outWriter.flush();
			outWriter.close();
		}
	}
	
	/**
	 * 
	 * @param entry - Map to be written to file
	 */
	public void writeMapToFile(Map<Integer, Map<String, List>> entry) {
		BufferedWriter bufferedWrite = null;
		try {
			bufferedWrite = new BufferedWriter(new FileWriter(App.DATA_DIR + App.FILE_BASE + "" + ++App.FILE_INDEX + ".csv", true));
			
			// get the years as an Integer array in sorted order
			Integer[] outerKey = entry.keySet().toArray(new Integer[entry.keySet().size()]);
			Arrays.sort(outerKey);
			
			// the innerMap's key is a date range for each season i.e. 01/01-03/20
			Set<String> innerKeySet;
			for(int i=0; i < outerKey.length; ++i) {
				innerKeySet = entry.get(outerKey[i]).keySet();
				String[] innerKey = innerKeySet.toArray(new String[innerKeySet.size()]);
				Arrays.sort(innerKey);
				for(int j=0; j < innerKey.length; ++j) {
					
					// get the season that the date range represents in words
					String season = ServerConfiguration.getConfiguration(innerKey[j]);
					//System.out.println("The inner key is " + innerKey[j] + " and the season is " + season);
					bufferedWrite.write(outerKey[i] + " ( " + season + " ),");
					List values = entry.get(outerKey[i]).get(innerKey[j]);
					for(int k=0; k < values.size(); ++k) {
						bufferedWrite.write(values.get(k).toString());
						if(k != values.size() - 1) {
							bufferedWrite.write(",");
						}
					}
					//log.info(key + " ---> " + innerKey + "  :  " + entry.get(key).get(innerKey).toString());
					bufferedWrite.write(System.getProperty("line.separator"));;
				}
			}
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		try {
			bufferedWrite.flush();
			bufferedWrite.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param newApp - An instance of the App class to run the application locally
	 * @return - Exit code, 0 for all going well, and 1 for an error occuring
	 */
	public static int runLocal(App newApp) {
		/*
		 *  Parsing the file for data
		 */
        File newFile = new File(App.DATA_DIR + App.FILE_SEPARATOR + App.FILENAME);
        InputStreamReader inputStream = null;
        try {
			inputStream = new InputStreamReader(new FileInputStream(newFile));
			System.out.println("Reading input file");
		} catch (FileNotFoundException e) {
			log.error("Ensure you have the right file and directory for the data");
			e.printStackTrace();
		}
	/*
	 *  Read the data file and store the value in a Map
	 *  using the date:time String as Map key       
	 */
        BufferedReader bufferedRead = new BufferedReader(inputStream);
        String data;
        int entries = 0;
        Float[] accumulated = null;
        boolean firstRunWithKey = true;
        String lastKey = null;
        try {
			while((data = bufferedRead.readLine()) != null) {
				String[] meterReadings = data.split(";");
				
				// check if the reading is the heading and skip
				if("Date".equals(meterReadings[0])) 
					continue;
				
				// check if the reading is empty and skip line
				if(newApp.isEmpty(meterReadings)) 
					continue;
				
	/*
	 *  Creating the key String, Map of the Float variables and 
	 *  Compute daily averages for each data entry 
	 *  meterReadings[0] is the date
	 *  meterReadings[1] is the time-stamped per minute
	 */				
				
				if(!cleanedMap.containsKey(meterReadings[0])) {
					
					// if the current key has at least one iteration, compute average of the 
					if(!firstRunWithKey) {
						accumulated = newApp.avgLists(accumulated, entries);
						newApp.writeToFile(lastKey, accumulated);
						cleanedMap.put(lastKey, accumulated);
					}
					entries = 0;
					
					// the first 2 meterReadings are Date and Time fields, others are the values to predict
					accumulated = new Float[meterReadings.length - Integer.parseInt(ServerConfiguration.getConfiguration("columns.to.skip"))];
					accumulated = newApp.initArray(accumulated);
					accumulated = newApp.sumLists(accumulated, meterReadings);
					lastKey = meterReadings[0];
					cleanedMap.put(lastKey, accumulated);
					firstRunWithKey = false;
					++entries;
				}
				else {
					accumulated = newApp.sumLists(accumulated, meterReadings);
					++entries;
				}
			}
			
			// accumulate the last key read and save in map
			accumulated = newApp.avgLists(accumulated, entries);
			newApp.writeToFile(lastKey, accumulated);
			cleanedMap.put(lastKey, accumulated);
		} catch (IOException e) {
			log.error("Unable to read from file: ", App.DATA_DIR + App.FILENAME);
				e.printStackTrace();
				return 1;
			}
	        
	        try {
	        	inputStream.close();
				bufferedRead.close();
			} catch (IOException e) {
				e.printStackTrace();
				return 1;
			}
	        return 0;
}	
	
			    public static void main( String[] args )
			    {
			    	//BasicConfigurator.configure();
			    	String appName = "Default";
			    	App newApp = null;
			    	int variableToPredict = 0;
			    	int numOfYearsToPredict = 1;
			    	if(args.length == 3) {
			    		System.out.println("Run: java -jar <program.jar> <work_directory (optional)> <variable_to_predict> <num_of_years>");
			    		App.DATA_DIR = args[0];
			    		
			    		// get the variables for prediction
			    		variableToPredict = Integer.parseInt(args[1]);
			    		numOfYearsToPredict = Integer.parseInt(args[2]);		
			    		System.out.println("OUT: Working directory is : " + App.DATA_DIR);
			    		log.info("Working directory is : " + App.DATA_DIR);
			    		newApp = new App();
			    		int exitCode = runLocal(newApp);
			    		if(exitCode == 1) {
			    			log.error("An error occured while reading the input file : " + App.DATA_DIR + App.FILENAME);
			    			System.exit(1);
			    		}
			    	}
			    	else if (args.length == 2) {
			    		appName = args[1];
			    		newApp = new App(appName);
			    		System.out.println( App.DEFAULT_DOMAIN_DESCRIPTION );
			    	}else {
			    		// bad input
			    	}
			
			/*
			 *  Checking to see if Map has been created      
			 */
			        System.out.println("*****************************************");
			        System.out.println("The size of Free Memory is : " + newApp.getMB(Runtime.getRuntime().freeMemory()));
			        System.out.println("*****************************************");    
			        System.out.println(cleanedMap.size());
			        
			        String mont;
			        int configSeasons = Integer.parseInt(ServerConfiguration.getConfiguration("seasons"));
			        if((Integer) configSeasons == null) {
			        	log.error("The value for seasons has not been set in the " + App.PROPERTIES_FILE + " file");
			        	System.exit(1);
			        }
			        System.out.println("The seasons are : ");
			        for(int i = 1; i <= configSeasons; ++i) {
			        	System.out.println("season" + i + " : " + ServerConfiguration.getConfiguration("season" + i));
			        }
			        
			        /*
			         *  Extract values from the output file and aggregate into 
			         *  A Map of a map. With the inner map the list of values
			         *  and the outer map the map of months and days 
			         */
	        
			        // read file to extract tokens
			        String daily = null;
			        Map<Integer, Map<Integer, List>> dateValues = new ConcurrentHashMap<Integer, Map<Integer, List>>();
			        Map<Integer, List> monthMap = null;


			        File preprocessedFile = new File(App.DATA_DIR + App.FILE_BASE + App.FILE_INDEX + ".csv");
			        BufferedReader preprocessedFileRead = null;
			        try {
			        	preprocessedFileRead = new BufferedReader(new InputStreamReader(new FileInputStream(preprocessedFile)));
					} catch (FileNotFoundException e) {
						log.error("Unable to open file " + preprocessedFile.getAbsolutePath() + ". ");
						log.error("Check if the file exists");
						e.printStackTrace();
					}
			        
			        try {
			        	// date[0] - day
			        	// date[1] - month
			        	// date[2] - year
			        	// chunks[1:end] - float values of readings
						while((daily = preprocessedFileRead.readLine()) != null) {
							String[] chunks = daily.split(",");
							String[] date = chunks[0].split("/");
		
							// check if the year key has been stored
							monthMap = (dateValues.containsKey(Integer.parseInt(date[2]))) ? dateValues.get(Integer.parseInt(date[2])) : new ConcurrentHashMap<Integer, List>();
							List<Float> values = new ArrayList<Float>();
							for(int index = 1; index < chunks.length; ++index) {
								values.add(Float.parseFloat(chunks[index]));					
							}
							
							// convert month/day to integer (month * 100 + day) and store as key in inner map
							// conversion is used for comparison with date ranges
							monthMap.put(SeasonalCalculator.getIntFromStringDate(date[1] + "/" + date[0]), values);
							dateValues.put(Integer.parseInt(date[2]), monthMap);
							//log.info(date[2] + "/" + date[1] + "/" + date[0] + ":" + values.toString());
						}
					} catch (IOException e) {
						log.error("Unable to read the file " + preprocessedFile.getAbsolutePath());
						e.printStackTrace();
					}
			        try {
						preprocessedFileRead.close();
					} catch (IOException e) {

						e.printStackTrace();
					}
		        
			        Map<Integer, Map<String, List>> aggregatedSeasonalLists = SeasonalCalculator.aggregateSeasons(dateValues);
			        newApp.writeMapToFile(aggregatedSeasonalLists);	    
			        
			        //predict values for all seasons for num of years
			        Vector<Float> predictedValues = Predictor.predict(numOfYearsToPredict, variableToPredict, aggregatedSeasonalLists);
			        System.out.println("Predicted values are " + predictedValues.toString());
			        
			        try {
						PrintWriter printOut = new PrintWriter(new File(App.DATA_DIR + App.FILE_BASE + "" + ++App.FILE_INDEX + ".csv"));
						int yearEnd = 2010;
						for(int i=1; i <= numOfYearsToPredict; ++ i) {
							printOut.write("         ");
							printOut.print(yearEnd + i);
							System.out.print("         ");
							System.out.print(yearEnd + i);

						}
						printOut.write(System.getProperty("line.separator"));
						System.out.println();
						int seasons = Integer.parseInt(ServerConfiguration.getConfiguration("seasons"));
						for(int j=0; j < seasons; ++j) {
							printOut.write("Season " + (j+1) + "  ");
							System.out.print("Season " + (j+1) + "  ");
							for(int i=0; i < numOfYearsToPredict; ++ i) {
								printOut.write(predictedValues.get(i*seasons + j).toString() + "  ");
								System.out.print(predictedValues.get(i*seasons + j).toString() + "  ");
							}
							printOut.write(System.getProperty("line.separator"));
							System.out.println();
						}
						printOut.flush();
						printOut.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    } //end of main
}
