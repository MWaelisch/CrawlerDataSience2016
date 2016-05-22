package main;

import java.util.ArrayList;
import java.util.Properties;

import model.Vip;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter4jWrapper {
	
	private Twitter twitter;
	
	public Twitter4jWrapper(Properties config){
		
		String apikey = config.getProperty("apikey");
		String apiSecret = config.getProperty("apisecret");
		String accessToken = config.getProperty("accessToken");
		String accessTokenSecret = config.getProperty("accessTokenSecret");
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(apikey)
		  .setOAuthConsumerSecret(apiSecret)
		  .setOAuthAccessToken(accessToken)
		  .setOAuthAccessTokenSecret(accessTokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
        
	}
	
	public void updateStatus(){
	    Status status;
		try {
			status = twitter.updateStatus("Hello API");
			System.out.println("Successfully updated the status to [" + status.getText() + "].");
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Possible requests 60/ 15 min
	 * 
	 * @param screenNames
	 */
	public ArrayList<Vip> lookupUsers(String[] screenNames){
		
		ArrayList<Vip> vips = new ArrayList<Vip>();
        try {
            ResponseList<User> users = twitter.lookupUsers(screenNames);
            for (User user : users) {
                    System.out.println("@" + user.getScreenName());
                    System.out.println("ID " + user.getId());
                    System.out.println("Name " + user.getName());
                    System.out.println("FollowerCount " + user.getFollowersCount());    
                    System.out.println("");
                    
                    Vip vip = new Vip();
                    vip.setId(user.getId());
                    vip.setAtName(user.getScreenName());
                    vip.setUserName(user.getName());
                    vip.setFollowerCount(user.getFollowersCount());
                    vips.add(vip);
            }       
            return vips;
            
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to lookup users: " + te.getMessage());
            System.exit(-1);
        }
		return null;
		
	}
	
	/**
	 * Possible Requests 30/ 15min
	 * 
	 * @param screenName
	 */
	public long[] getFriendsIDs(String screenName){
        try {
            long cursor = -1;
            IDs ids;
            do {
                ids = twitter.getFriendsIDs(screenName, cursor);
//                System.out.println(ids.getIDs().length);
//                for (long id : ids.getIDs()) {
//                    System.out.println(id);
//                }
                return ids.getIDs();
            } while ((cursor = ids.getNextCursor()) != 0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get friends' ids: " + te.getMessage());
            System.exit(-1);
        }
		return null;
	}
	
//	public void getFriends(){
//	      long cursor = -1;
//	      IDs ids;
//	      System.out.println("Listing followers's ids.");
//	      do {
//	              ids = twitter.getFollowersIDs("username", cursor);
//	          for (long id : ids.getIDs()) {
//	              System.out.println(id);
//	              User user = twitter.showUser(id);
//	              System.out.println(user.getName());
//	          }
//	      } while ((cursor = ids.getNextCursor()) != 0);
//	}

}
