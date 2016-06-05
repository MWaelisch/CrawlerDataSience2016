package util;

import java.io.File;

public class Utility {
	
	
	public static void makeDir(String directoryName){
		File theDir = new File(directoryName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + directoryName);
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
	}

}
