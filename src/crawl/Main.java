package crawl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import util.*;

public class Main {
	
	public static void main(String[] args){
		Properties config = getConfig();
		Database database = new Database();
		Twitter4jWrapper wrapper = new Twitter4jWrapper(config, database);
		CSVParser parser = new CSVParser(config);
		ArrayList<String[]> vipNickNames = parser.parseVipNickNames();
		ArrayList<String> vipNames = parser.getVipNames(vipNickNames);
		System.out.println("Vip Listen erfolgreich erstellt");
//		wrapper.checkRateLimit("/friends/ids");
//		wrapper.crawlVips(vipNames);
//		wrapper.crawlVipTweets(vipNames);
//		wrapper.searchTweets(vipNickNames);	
//		database.getAllTweetsfromDB("plebTweets");
//		wrapper.crawlPlebFriends(0, 5);
		database.closeConnection();
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
