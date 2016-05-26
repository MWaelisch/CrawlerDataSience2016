package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class CSVParser {
	private static final int VIP_NAME = 0;
	private static final int VIP_AT_NAME = 1;
	private static final int VIP_HASH_NAME = 2;
	
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
	

	public ArrayList<String[]> parseVipNickNames() {

		ArrayList<String[]> nameArrays = new ArrayList<String[]>();
		BufferedReader fileReader = null;
		// Delimiter used in CSV file
		final String DELIMITER = ";";
		try {
			String line = "";
			// Create the file reader
			fileReader = new BufferedReader(new FileReader(config.getProperty("vipCsv")));

			while ((line = fileReader.readLine()) != null) {
				String[] tokens = line.split(DELIMITER, 1);

				String[] namesSplit = tokens[0].split(",");
				String[] names = new String[3];
				names[VIP_NAME] = namesSplit[VIP_NAME].trim();
				names[VIP_NAME] = names[VIP_NAME].substring(1, names[VIP_NAME].length());
				names[VIP_AT_NAME] = namesSplit[VIP_AT_NAME].trim();
				names[VIP_AT_NAME] = names[VIP_AT_NAME].substring(0, names[VIP_AT_NAME].length()-1);
				names[VIP_HASH_NAME] = "#"+names[VIP_AT_NAME].substring(1);
				System.out.println("=== " + names[VIP_NAME] + " - " + names[VIP_AT_NAME] + " - " + names[VIP_HASH_NAME]);
				nameArrays.add(names);
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
		return nameArrays;
	}
	
	public ArrayList<String> getVipNames(ArrayList<String[]> vipNickNames) {
		ArrayList<String> vipNames = new ArrayList<String>();
		
		for(String[] vipNickName : vipNickNames){
			vipNames.add(vipNickName[VIP_AT_NAME].substring(1));
		}
		
		return vipNames;
	}

}
