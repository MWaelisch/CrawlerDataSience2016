package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import model.Vip;
import model.VipTweet;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter4jWrapper {
	
	private Twitter twitter;
	private Properties config;
	
	public Twitter4jWrapper(Properties config){
		
		this.config = config;
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
	
	
	public void crawlVips(ArrayList<String> vipNames)
	{
		List<List<String>> parts = prepareVipLists(vipNames, 100);
		
		Database database = new Database();
		
		for(List<String> part : parts){
			
			String[] vipnamesArr = new String[part.size()];
			vipnamesArr = part.toArray(vipnamesArr);
			
			System.out.println("Crawle Userdaten der Vips");
			ArrayList<Vip> vips = this.lookupUsers(vipnamesArr);
			System.out.println("Userdaten gecrawled");
			
			System.out.println("Crawle Friends der Vips");
			for(Vip vip : vips){
				System.out.println("Crawle Friends von " + vip.getScreenName());
				long[] friends = this.getFriendsIDs(vip.getScreenName());
				vip.setFriends(friends);
				database.addVip(vip);
				try {
					Thread.sleep(61000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					database.closeConnection();
				}
			}
			System.out.println("Friends gecrawled");
			
		}
		database.closeConnection();
		System.out.println("Finished");
	}
	
	
	public <T> List<List<T>> prepareVipLists(List<T> list, final int L) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += L) {
	        parts.add(new ArrayList<T>(
	            list.subList(i, Math.min(N, i + L)))
	        );
	    }
	    return parts;
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
                    vip.setScreenName(user.getScreenName());
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
	 * Possible Requests 15/ 15min
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
	
	public void crawlVipTweets(List<String> vips){
		try {

			for (String vip : vips) {
				List<Status> statuses = null;

				Paging page = new Paging(1, 100);// page number, number per page
				for (int i = 1; i < 2; i++) {
					page.setPage(i);
					if (i == 1) {
						statuses = twitter.getUserTimeline(vip, page);
					} else {
						statuses.addAll(twitter.getUserTimeline(vip, page));
					}
					Thread.sleep(3000);
				}

				Database database = new Database();
				for (Status status : statuses) {
					VipTweet vipTweet = new VipTweet();
					vipTweet.setIdStr(status.getId() + "");
					vipTweet.setAuthorId(status.getUser().getId());
					vipTweet.setAuthorName(status.getUser().getScreenName());
					vipTweet.setInReplyTo(status.getInReplyToUserId());

					UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
					long[] userMentions = new long[userMentionEntities.length];
					for (int i = 0; i < userMentionEntities.length; i++) {
						long userMention = userMentionEntities[i].getId();
						System.out.println(userMentionEntities[i].getScreenName());
						userMentions[i] = userMention;
					}
					vipTweet.setMentions(userMentions);
					vipTweet.setText(status.getText());
					if(status.getRetweetedStatus() != null){
						vipTweet.setRetweetOrigin(status.getRetweetedStatus().getUser().getId());
						System.out.println("Retweeted Text" + status.getRetweetedStatus().getText());
						System.out.println("Origin Text " + status.getRetweetedStatus().getText());
						System.out.println("Retweet Origin:" + status.getRetweetedStatus().getUser().getId());
						System.out.println("Rwetweet Origin Name: " +  status.getRetweetedStatus().getUser().getName());
						System.out.println("In Reply to:" + status.getInReplyToUserId());
					}
					database.addVipTweet(vipTweet);
				}
			}

		} catch (TwitterException | InterruptedException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		}

	}

}
