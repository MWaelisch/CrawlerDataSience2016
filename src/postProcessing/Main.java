package postProcessing;

import model.Tweet;
import model.VipTweet;
import util.Database;

import java.util.ArrayList;

public class Main {
	
	
	public static void main(String [] args) {

        //get VipTweets from db
        SentiStrengthWrapper sentiStrength = new SentiStrengthWrapper();
        Integer[] sentiScore;
        Database database = new Database();
//        ArrayList<Tweet> vipTweets = database.getAllTweetsfromDB("vipTweets");

        //debugging
//        for (int i = 0; i < 4; i++) {
//            System.out.println(vipTweets.get(i).getAuthorId()+": "+vipTweets.get(i).getText()+" || "+
//            vipTweets.get(i).getSentimentPos());
//        }

        //calculate sentiScore for each tweet
//        for(Tweet vt : vipTweets){
//            sentiScore = sentiStrength.getSentiScores(vt.getText());
//            vt.setSentiScore(sentiScore);
//        	
//        	//debug
//            System.out.println(vt.getAuthorId()+": "+vt.getText()+" *** "+
//                  vt.getSentimentPos() + " || " + vt.getSentimentNeg());
//        }

        //debugging
//        for (int i = 0; i < 4; i++) {
//            System.out.println(vipTweets.get(i).getAuthorId()+": "+vipTweets.get(i).getText()+" || "+
//                    vipTweets.get(i).getSentimentPos());
//        }

        //write sentiScore in vipTweets table in db
//        database.updateTweets(vipTweets,"vipTweets");
//        
        ArrayList<Tweet> plebTweets = database.getAllTweetsfromDB("plebTweets");
//        
        for(Tweet pt : plebTweets){
//        	sentiScore = sentiStrength.getSentiScores(pt.getText());
//        	pt.setSentiScore(sentiScore);
        
        	//debug
        	System.out.println(pt.getAuthorId()+": "+pt.getText()+" *** +"+
                  pt.getSentimentPos() + " || " + pt.getSentimentNeg());
        }
//        
//        database.updateTweets(plebTweets,"plebTweets");
        
//        System.out.println(database.getAllTweetsfromDB("plebTweets").size());
//        database.cleanPlebTweets();
//        System.out.println(database.getAllTweetsfromDB("plebTweets").size());
        database.closeConnection();
		System.out.println("Clear");
		
	}

}
