package postProcessing;

import uk.ac.wlv.sentistrength.*;

public class SentiStrengthWrapper {
	
	public SentiStrengthWrapper() {}
	
	public void testSenti(){

		//Method 2: One initialisation and repeated classifications
		SentiStrength sentiStrength = new SentiStrength(); 
		//Create an array of command line parameters to send (not text or file to process)
		String ssthInitialisation[] = {"sentidata", "SentiData/sentistrength_de/"};
		sentiStrength.initialise(ssthInitialisation);
		System.out.println(sentiStrength.computeSentimentScores("Ich hasse frösche.")); 
		System.out.println(sentiStrength.computeSentimentScores("I liebe hunde.")); 
	}


}
