/**
 * 
 */
package bigdata.TimeSeriesAnalysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adeyemi
 *
 */
public class ServerConfiguration {
	protected static Logger log = LoggerFactory.getLogger(ServerConfiguration.class);
	
	protected static Map<String, String> configMap;
	
	public ServerConfiguration() {
		ServerConfiguration.configMap = new ConcurrentHashMap<String, String>();
		ServerConfiguration.init();
	}
	
	private static void init() {
		ServerConfiguration.readConfigFile("./resources/");
	}
	
	/**
	 * 
	 * @param fileDir - String directory name to search for properties files
	 * @return
	 */
	private static Map<String, String> readConfigFile(String fileDir) {
		Path fileDirPath = FileSystems.getDefault().getPath(fileDir);
		Map<String, String> tmpMap = new ConcurrentHashMap<String, String>();

		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(fileDirPath)) {
			BufferedReader bufferedRead = null;
			String line = null;
			for(Path pathName : dirStream) {
				if(pathName.toString().endsWith(".properties") || pathName.toString().endsWith(".txt")) {
					try {
						bufferedRead = new BufferedReader(new FileReader(pathName.toString()));
						while( (line = bufferedRead.readLine()) != null) {
							String[] configSettings = line.split("=");
							if(configSettings.length == 2) {
								tmpMap.put(configSettings[0].trim(), configSettings[1].trim());
								System.out.println(configSettings[0].trim() + " = " + configSettings[1].trim());
							}
						}
					} catch (FileNotFoundException e) {
						log.error("Ensure the file exists");
						System.out.println("Ensure the file exists");
						e.printStackTrace();
					} catch (IOException e) {
						log.error("Ensure the file is readable");
						System.out.println("Ensure the file is readable");				
						e.printStackTrace();
					}
					finally {
						try {
							bufferedRead.close();
						} catch (IOException e) {
							log.error("Ensure the file is not ");
							System.out.println("Ensure the file exists");
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e1) {
			System.out.println("Ensure the Directory exists and is not closed");
			e1.printStackTrace();
		}
		return tmpMap;
	}
	
	/**
	 * 
	 * @param property - the property string to extract from the server configuration
	 * @return value - The value that the key maps to
	 */
	public static String getConfiguration(String property) {
		return (ServerConfiguration.configMap.containsKey(property)) ? ServerConfiguration.configMap.get(property) : null;
	}
	
	/**
	 * 
	 * @param properties - a list of properties to extract from the server configuration
	 * @return Map<Property, Value> - A map of properties corresponding to the values of the properties
	 * 			Properties not defined in the configuration returns a null value
	 */
	public static Map<String, String> getConfiguration(List<String> properties) {
		Map<String, String> tmpMap = new ConcurrentHashMap<String, String>();
		for(String property : properties) {
			tmpMap.put(property, (configMap.containsKey(property)) ? configMap.get(property) : null);
		}
		return tmpMap;
	}
}
