package crawl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import model.Vip;
import postProcessing.SentiStrengthWrapper;
import util.*;

public class Main {

	public static void main(String[] args) {
		Properties config = getConfig();
		try {
			//create subdirectory for the databases
			Utility.makeDir("databases");
			
		    if(args.length == 0 || args.length == 1)
		    {
		    	System.out.println("!!WRONG USAGE!!");
		    	//TODO explain proper usage
		        System.out.println("Proper Usage is: java -jar commando parameter ");
		        System.exit(0);
		    }
		    
			// crawl all vip data and clean the database
			// example call:  java -jar testScience.jar crawl HipHopVips.csv
			if (args[0].equals("crawl")) {
				Database rawDatabase = new Database("databases/raw.db");
				Twitter4jWrapper wrapper = new Twitter4jWrapper(config, rawDatabase);
				CSVParser parser = new CSVParser(args[1]);
				ArrayList<String> vipNames = parser.parseVips();
				SentiStrengthWrapper sentiStrength = new SentiStrengthWrapper();
				for(String vipName : vipNames){
					Vip vip = wrapper.crawlVip(vipName);
					wrapper.crawlVipTweets(vip);
					wrapper.searchTweets(vip);
					wrapper.crawlPlebFriends();
				}
				//calculate the SentiScore for all Tweets
				sentiStrength.setDatabase(rawDatabase);
				sentiStrength.calculateSentiScore("plebTweets");
				sentiStrength.calculateSentiScore("vipTweets");
				rawDatabase.closeConnection();
				
//	            //delete current clean version of db if exists
//				File rawDb = new File("databases/raw.db");
//	            File cleanedDb = new File("databases/cleaned.db");
//	            if(cleanedDb.exists()) { 
//            	   FileUtils.forceDelete(cleanedDb);
//	            }
////				//copy database to save raw version
//				FileUtils.copyFile(rawDb, cleanedDb);
//				//establish connection to the new cleaned db 
//				Database cleanedDatabase = new Database("databases/cleaned.db");
//				//make the database clean
//				cleanedDatabase.cleanVips();
////				database.cleanProtectedPlebs();
//				cleanedDatabase.closeConnection();
				System.out.println("---------FINISH---------");
			}
			
		} catch (SQLException | IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Properties getConfig() {
		Properties props = new Properties();
		InputStream stream = Twitter4jWrapper.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			props.load(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props;
	}

}
