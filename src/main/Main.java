package main;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
	
	public static void main(String[] args){
		Properties config = getConfig();
		Twitter4jWrapper wrapper = new Twitter4jWrapper(config);
//		wrapper.updateStatus();
		wrapper.lookupUsers(new String[] {"marteria", "prinzpi23", "YSLPlug"});
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
