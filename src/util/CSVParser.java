package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class CSVParser {
	
	private Properties config;
	
	public CSVParser(Properties config){
		
		this.config = config;
		
	}
	
	
	public ArrayList<String> parseVips() {

		ArrayList<String> names = new ArrayList<String>();
		BufferedReader fileReader = null;
		// Delimiter used in CSV file
		final String DELIMITER = ";";
		try {
			String line = "";
			// Create the file reader
			fileReader = new BufferedReader(new FileReader(config.getProperty("vipCsv")));

			while ((line = fileReader.readLine()) != null) {
				String[] tokens = line.split(DELIMITER, 1);
				//System.out.println(tokens[0]);

				String[] namesSplit = tokens[0].split(",");
				String atName = namesSplit[1].trim();
				atName = atName.substring(1,atName.length()-1);
				//System.out.println(atName);
				names.add(atName);
			}

			fileReader.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return names;
	}

}
