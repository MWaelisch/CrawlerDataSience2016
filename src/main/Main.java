package main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import model.Vip;

public class Main {
	
	public static void main(String[] args){
		Properties config = getConfig();
		Twitter4jWrapper wrapper = new Twitter4jWrapper(config);
//		wrapper.updateStatus();
		ArrayList<Vip> vips = wrapper.lookupUsers(new String[] {"marteria", "prinzpi23", "YSLPlug"});
		for(Vip vip : vips){
			long[] friends = wrapper.getFriendsIDs("marteria");
			vip.setFriends(friends);
		}
		System.out.println("Finished");
		
		Database database = new Database();
		for(Vip vip : vips){
			database.addVIP(vip);
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
