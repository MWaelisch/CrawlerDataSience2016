package postProcessing;

import uk.ac.wlv.sentistrength.*;

public class SentiStrengthWrapper {
	
	private SentiStrength sentiStrength;
	
	public SentiStrengthWrapper() {
		this.sentiStrength = new SentiStrength(); 
		//Create an array of command line parameters to send (not text or file to process)
		String ssthInitialisation[] = {"sentidata", "SentiData/sentistrength_de/"};
		sentiStrength.initialise(ssthInitialisation);
	}

	public Integer[] getSentiScores(String text){
		String[] stringResult = sentiStrength.computeSentimentScores(text).split(" ");
		//todo make failsafe
		return new Integer[]{Integer.parseInt(stringResult[0]),Integer.parseInt(stringResult[1])};
	}

	public void testSenti(){
		System.out.println(sentiStrength.computeSentimentScores("Das wetter ist schön.")); 
		System.out.println(sentiStrength.computeSentimentScores("I liebe hunde nicht ich hasse sie.")); 
	}
}
