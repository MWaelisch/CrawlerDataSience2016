package main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import util.*;

public class Main {
	
	public static void main(String[] args){
		Properties config = getConfig();
		Twitter4jWrapper wrapper = new Twitter4jWrapper(config);
		CSVParser parser = new CSVParser(config);
		ArrayList<String> vipNames = parser.parseVips();
		System.out.println("Vip Liste erfolgreich erstellt");
//		wrapper.crawlVips(vipNames);
		wrapper.crawlVipTweets(vipNames);
		
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
