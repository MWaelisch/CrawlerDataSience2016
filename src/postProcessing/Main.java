package postProcessing;

import model.VipTweet;
import util.Database;

import java.util.ArrayList;

public class Main {
	
	
	public static void main(String [] args) {

        //get VipTweets from db
        SentiStrengthWrapper sentiStrength = new SentiStrengthWrapper();
        Integer[] sentiScore;
        Database database = new Database();
        ArrayList<VipTweet> vipTweets = database.getAllVIPTweetsfromDB();

        //debugging
        for (int i = 0; i < 4; i++) {
            System.out.println(vipTweets.get(i).getAuthorId()+": "+vipTweets.get(i).getText()+" || "+
            vipTweets.get(i).getPosSentiment());
        }

        //calculate sentiScore for each tweet
        for(VipTweet vt : vipTweets){
            sentiScore = sentiStrength.getSentiScores(vt.getText());
            vt.setSentiScore(sentiScore);
        }

        //debugging
        for (int i = 0; i < 4; i++) {
            System.out.println(vipTweets.get(i).getAuthorId()+": "+vipTweets.get(i).getText()+" || "+
                    vipTweets.get(i).getPosSentiment());
        }

        //write sentiScore in vipTweets table in db
        database.updateTweets(vipTweets,"vipTweets");

        database.closeConnection();
		System.out.println("Clear");
		
	}

}
