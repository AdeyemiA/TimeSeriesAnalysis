package bigdata.TimeSeriesAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	
	protected Map<String, Float[]> cleanedMap;
	
	protected static String FILENAME = "household_power_consumption.txt";
	
	protected static int FILE_INDEX = 0;
	
	protected static String FILE_BASE = "Out";
	
	// default directory if running local
	protected static String DATA_DIR = "/tmp/";
	
	protected static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	// no-args constructor
	public App() {
		this("TimeSeries");
	}
	
	public App(String projectName) {
		super(projectName);
		App.NAME = projectName;
		this.textMap =  Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Float[]>>());
		this.cleanedMap = Collections.synchronizedMap(new LinkedHashMap<String, Float[]>());
	}
	
	public String getName() {
		return App.NAME;
	}
	
	public boolean isEmpty(String[] array) {
		for(int i=0; i < array.length - 1; ++i) {
			if(array[i] == null || "?".equals(array[i]) || array[i] == "")
					return true;
		}
		return false;
	}
	
	public Float getMB(Long num) {
		return (float) (num/ (1024.0*1024.0));
	}
	
	public Float[] sumLists(Float[] cumulative, Float[] entry) {
		for(int i=0; i < entry.length; ++i) {
			cumulative[i] = cumulative[i] + entry[i]; 
		}
		return cumulative;
	}
	
	public Float[] sumLists(Float[] cumulative, String[] entry) {
		for(int i=0; i < cumulative.length; ++i) {
			cumulative[i] = cumulative[i] + Float.parseFloat(entry[i+2]); 
		}
		return cumulative;
	}
	
	public Float[] avgLists(Float[] cumulative, int entries) {
		for(int i=0; i < cumulative.length; ++i) {
			cumulative[i] = cumulative[i] / entries; 
		}
		return cumulative;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T[] initArray(T[] arr) {
		for(int i=0; i < arr.length; ++i) {
			if(arr[i] instanceof String)
				arr[i] = (T) "";
			else if(arr[i] instanceof Integer)
				arr[i] = (T) new Integer(0);
			else if(arr[i] instanceof Float)
				arr[i] = (T) new Float(0.0f);
			else if(arr[i] instanceof Double)
				arr[i] = (T) new Double(0.0);
			else
				arr[i] = (T) new Object();
		}
		return arr;
	}
	
	public Float[] initArray(Float[] arr) {
		for(int i=0; i < arr.length; ++i) {
			arr[i] = new Float(0.0f);
		}
		return arr;
	}
	
	public <T> void writeToFile(String key, Float[] arr) throws IOException {
		BufferedWriter outWriter = null;
        try {
			outWriter = new BufferedWriter(new FileWriter(App.DATA_DIR + App.FILE_BASE + App.FILE_INDEX + ".csv", true));
			StringBuffer s;
				s = new StringBuffer();
				//String header = "Date,Global_active_power,Global_reactive_power,Voltage,Global_intensity,Sub_metering_1,Sub_metering_2,Sub_metering_3";
				//outWriter.write(header);
				//outWriter.write(System.getProperty("line.separator"));
				for(int i=0; i < arr.length; ++i) {
					
					s.append(arr[i]);
					if(i != arr.length - 1) {
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
        Float[] arr = null;
        boolean sameKeyGrouping = false;
        boolean firstRunWithKey = true;
        String lastKey = null;
        try {
			Files.deleteIfExists(FileSystems.getDefault().getPath("data", "Out*"));
		} catch (IOException e1) {

			e1.printStackTrace();
		}
        try {
			while((data = bufferedRead.readLine()) != null) {
				String[] meterReadings = data.split(";");
				if("Date".equals(meterReadings[0])) 
					continue;
				if(newApp.isEmpty(meterReadings)) 
					continue;
				
	/*
	 *  Creating the key String, Map of the Float variables and 
	 *  Compute daily averages for each data entry 
	 *  meterReadings[0] contains the date
	 *  meterReadings[1] contains the time every minute
	 */				
				
				sameKeyGrouping = false;
				if(!newApp.cleanedMap.containsKey(meterReadings[0])) {
					if(!firstRunWithKey) {
						arr = newApp.avgLists(arr, entries);
						newApp.writeToFile(lastKey, arr);
						newApp.cleanedMap.put(lastKey, arr);
					}
					entries = 0;
					arr = new Float[7];
					arr = newApp.initArray(arr);
					arr = newApp.sumLists(arr, meterReadings);
					lastKey = meterReadings[0];
					newApp.cleanedMap.put(lastKey, arr);
					firstRunWithKey = false;
					++entries;
				}
				else {
					sameKeyGrouping = true;
					arr = newApp.sumLists(arr, meterReadings);
					++entries;
				}
			}
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
			    	if(args.length == 1) {
			    		System.out.println("Run: java -jar <program.jar> <TimeSeriesName>");
			    		App.DATA_DIR = args[0];
			    		System.out.println("OUT: Working directory is : " + App.DATA_DIR);
			    		log.info("Working directory is : " + App.DATA_DIR);
			    		newApp = new App();
			    		int exitCode = runLocal(newApp);
			    	}
			    	else if (args.length == 2) {
			    		appName = args[1];
			    		newApp = new App(appName);
			    		System.out.println( App.DEFAULT_DOMAIN_DESCRIPTION );
			    	}
			
			/*
			 *  Checking to see if Map has been created      
			 */
			        System.out.println("*****************************************");
			        System.out.println("The size of Free Memory is : " + newApp.getMB(Runtime.getRuntime().freeMemory()));
			        System.out.println("*****************************************");    
			        System.out.println(newApp.cleanedMap.size());
			        
			        
			        String mont = ServerConfiguration.getConfiguration("autumn");
			        System.out.println("The autumn months is : " + mont);
			        
			        /*
			         *  Extract values from the output file and aggregate into 
			         *  A Map of a map. With the inner map the list of values
			         *  and the outer map the map of months and days 
			         */
			        
			        File preprocessedFile = new File(App.DATA_DIR + App.FILE_BASE + App.FILE_INDEX + ".csv");
			        BufferedReader preprocessedFileRead = null;
			        try {
			        	preprocessedFileRead = new BufferedReader(new InputStreamReader(new FileInputStream(preprocessedFile)));
					} catch (FileNotFoundException e) {
						log.error("Unable to open file " + preprocessedFile.getAbsolutePath() + ". ");
						log.error("Check if the file exists");
						e.printStackTrace();
					}
			        
			        // read file to extract tokens
			        String daily = null;
			        Map<Integer, Map<String, List>> dateValues = new ConcurrentHashMap<Integer, Map<String, List>>();
			        Map<String, List> monthMap = null;
			        try {
						while((daily = preprocessedFileRead.readLine()) != null) {
							String[] chunks = daily.split(",");
							String[] date = chunks[0].split("/");
		
							// check if the year key has been stored
							monthMap = (dateValues.containsKey(date[2])) ? dateValues.get(date[2]) : new ConcurrentHashMap<String, List>();
							List<Float> values = new ArrayList<Float>();
							for(int index = 1; index < chunks.length; ++index) {
								values.add(Float.parseFloat(chunks[index]));					
							}
							// month/day
							monthMap.put(date[1] + "/" + date[0], values);
							dateValues.put(Integer.parseInt(date[2]), monthMap);
							log.info(date[2] + "/" + date[1] + "/" + date[0] + ":" + values.toString());
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
			        
			        
			    } //end of main
}
