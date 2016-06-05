package crawl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import util.*;

public class Main {

	public static void main(String[] args) {
		Properties config = getConfig();
		try {
			
		    if(args.length == 0 || args.length == 1)
		    {
		    	System.out.println("!!WRONG USAGE!!");
		        System.out.println("Proper Usage is: java -jar testScience.jar [command] [parameter]");
		        System.exit(0);
		    }
			// crawl all vip data
			// example call: java -jar jarName crawlVips pathToVipCsv
			if (args[0].equals("crawlVips")) {
				Database database = new Database("vipraw.db");
				Twitter4jWrapper wrapper = new Twitter4jWrapper(config, database);
				CSVParser parser = new CSVParser(args[1]);
				ArrayList<String> vipNames = parser.parseVips();
				wrapper.crawlVips(vipNames);
				database.closeConnection();
			}

			// ArrayList<String[]> vipNickNames = parser.parseVipNickNames();
			// System.out.println("Vip Listen erfolgreich erstellt");
			// wrapper.checkRateLimit("/friends/ids");

			// wrapper.crawlVipTweets(vipNames);
			// wrapper.searchTweets(vipNickNames);
			// database.getAllTweetsfromDB("plebTweets");
			// wrapper.crawlPlebFriends(2760, 2880);
			// database.executeQuery(""); //"SELECT * FROM plebFriends GROUP BY
			// pleb");
			// databse.cleanPlebFriends(); //after getting all!!
			// database.closeConnection();

		} catch (SQLException | IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
// old main method use for running in eclipse
//	public static void main(String[] args) {
//		Properties config = getConfig();
//		Database database;
//		try {
//			database = new Database("vipraw.db");
//
//			Twitter4jWrapper wrapper = new Twitter4jWrapper(config, database);
//			CSVParser parser = new CSVParser(config);
//			ArrayList<String[]> vipNickNames = parser.parseVipNickNames();
//			ArrayList<String> vipNames = parser.getVipNames(vipNickNames);
//			 System.out.println("Vip Listen erfolgreich erstellt");
//			 wrapper.checkRateLimit("/friends/ids");
//			 wrapper.crawlVips(vipNames);
//			 wrapper.crawlVipTweets(vipNames);
//			 wrapper.searchTweets(vipNickNames);
//			 database.getAllTweetsfromDB("plebTweets");
//			 wrapper.crawlPlebFriends(2760, 2880);
//			 database.executeQuery(""); //"SELECT * FROM plebFriends GROUP BY
//			 pleb");
//			 databse.cleanPlebFriends(); //after getting all!!
//			 database.closeConnection();
//
//		} catch (SQLException | IOException | ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

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
