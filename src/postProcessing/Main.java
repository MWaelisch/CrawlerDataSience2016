package postProcessing;

import util.Database;

public class Main {
	
	
	public static void main(String [] args) {
		
		Database database = new Database();
		database.cleanDB();
		System.out.println("Clear");
		
	}

}
