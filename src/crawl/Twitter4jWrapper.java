package crawl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import model.Pleb;
import model.Tweet;
import model.Vip;
import model.VipTweet;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;
import util.Database;

public class Twitter4jWrapper {
	
	private Twitter twitter;
	private Properties config;
	
	private Database database;
	
	public Twitter4jWrapper(Properties config, Database database){
		
		this.config = config;
		String apikey = config.getProperty("apikey");
		String apiSecret = config.getProperty("apisecret");
		String accessToken = config.getProperty("accessToken");
		String accessTokenSecret = config.getProperty("accessTokenSecret");
		
		this.database = database;
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(apikey)
		  .setOAuthConsumerSecret(apiSecret)
		  .setOAuthAccessToken(accessToken)
		  .setOAuthAccessTokenSecret(accessTokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
	
	public int checkRateLimit(String method){
			  try {
				RateLimitStatus status = twitter.getRateLimitStatus().get(method);
				int limit = status.getRemaining();
				return limit;
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
			  
	}
	
	public Vip crawlVip(String vipName) {
		try {
			//crawl only if vip is not already in DB
			if (!database.isVipInDb(vipName)) {
				System.out.println("Crawle Userdaten von " + vipName);
				Vip vip = this.lookupUser(vipName);
				System.out.println("Userdaten gecrawled");

				if (this.checkRateLimit("/friends/ids") <= 1) {
					System.out.println("Sleep 15 minutes...");
					Thread.sleep(901000);
				}
				System.out.println("Crawle Friends von " + vip.getScreenName());
				long[] friends = this.getFriendsIDs(vip.getScreenName());
				vip.setFriends(friends);
				database.addVip(vip);
				System.out.println("Friends gecrawled");

				System.out.println("Finished");
				return vip;
			}else{
				System.out.println("VIP " + vipName + " is already in DB");
				return database.getVIPfromDB(vipName);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Possible requests 60/ 15 min
	 * 
	 * @param screenName
	 */
	public Vip lookupUser(String screenName) {

		try {
			User user = twitter.showUser(screenName);
//			System.out.println("@" + user.getScreenName());
//			System.out.println("ID " + user.getId());
//			System.out.println("Name " + user.getName());
//			System.out.println("FollowerCount " + user.getFollowersCount());
//			System.out.println("Profilepicturemini " + user.getMiniProfileImageURL());
//			System.out.println("Profilepicture bigger" + user.getBiggerProfileImageURL());
//			System.out.println("Profilepicture" + user.getOriginalProfileImageURL());
//			System.out.println("Profilepicture" + user.getProfileImageURL());

			Vip vip = new Vip();
			vip.setId(user.getId());
			vip.setScreenName(user.getScreenName());
			vip.setUserName(user.getName());
			vip.setFollowerCount(user.getFollowersCount());
			vip.setProfilePicture(user.getProfileImageURL());

			return vip;

		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to lookup user: " + te.getMessage());
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
	
	/**
	 * Crawl Tweets from 
	 * a Vips User Timeline
	 * 
	 * @param vip
	 */
	public void crawlVipTweets(Vip vip) {
		try {

			System.out.println("Crawl Tweets for " + vip.getScreenName());
			List<Status> statuses = null;
			Paging page = new Paging(1, 200);// page number, number per page
			for (int i = 1; i <= 5; i++) { // debug config -> get only 1 page
				if (this.checkRateLimit("/statuses/user_timeline") == 0) {
					System.out.println("Sleep 15 minutes...");
					Thread.sleep(901000);
				}
				page.setPage(i);
				if (i == 1) {
					statuses = twitter.getUserTimeline(vip.getScreenName(), page);
				} else {
					statuses.addAll(twitter.getUserTimeline(vip.getScreenName(), page));
				}
			}
			System.out.println("Finished crawling Tweets for " + vip.getScreenName());

			System.out.println("Saving Tweets for " + vip.getScreenName());
			for (Status status : statuses) {
				VipTweet vipTweet = new VipTweet();
				vipTweet.setIdStr(status.getId() + "");
				vipTweet.setAuthorId(status.getUser().getId());
				vipTweet.setScreenName(status.getUser().getScreenName());
				vipTweet.setInReplyTo(status.getInReplyToUserId());

				UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
				Long[] userMentions = new Long[userMentionEntities.length];
				for (int i = 0; i < userMentionEntities.length; i++) {
					long userMention = userMentionEntities[i].getId();
					userMentions[i] = userMention;
				}
				vipTweet.setMentions(userMentions);
				vipTweet.setText(status.getText());
				if (status.getRetweetedStatus() != null) {
					vipTweet.setRetweetOrigin(status.getRetweetedStatus().getUser().getId());
				}
				database.addVipTweet(vipTweet);
			}
			System.out.println("Finished Saving Tweets for " + vip.getScreenName());

		} catch (TwitterException | InterruptedException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		}

	}
	

	/**
	 * Search tweets where 
	 * a VIP is mentioned
	 * 
	 * @param vip
	 */
	public void searchTweets(Vip vip) {
		System.out.println("Search for mentions of VIP " + vip.getScreenName());
		String q = "@" + vip.getScreenName() + " OR #" + vip.getScreenName() + " OR \"" + vip.getUserName() + "\"";
//		System.out.println("### vipname-query: " + q);
		searchTweet(q, vip.getScreenName());
		System.out.println("Finished searching for mentions of VIP" + vip.getScreenName());
	}


	/**
	 * Possible requests 180/ 15 min
	 * 	                 450/ 15 min app-only
	 * 
	 * @param searchTweets //todo this is throwing an error in my IDE?
	 * @return 
	 */
	private void searchTweet(String q, String screenName){
		try {
			   Query query = new Query(q);
	           QueryResult result;
               int count = 0;
               int leftToCrawl = twitter.getRateLimitStatus().get("/search/tweets").getRemaining();
	           do {
	        	   if(leftToCrawl <= 0){
		               try {
		            	   System.out.println("Asleep for 15 minutes ...");
							//180 bzw 450 requests / 15 min
							Thread.sleep(901000);
							leftToCrawl = twitter.getRateLimitStatus().get("/search/tweets").getRemaining();
		               } catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
		               }
	        	   }
	        	   
	               result = twitter.search(query);
	        	   leftToCrawl--;
	               List<Status> tweets = result.getTweets();
	               
	               	//debug
	               	//System.out.println("tweets: " + tweets.toString());
	               for (Status tweet : tweets) {
//	            	   System.out.println("Language: " + tweet.getLang() + "   " + tweet.getText());
	            	   if(tweet.getLang().matches("de|en")){
	            		   if(database.isTweetInDb(String.valueOf(tweet.getId()),"plebTweets")){
	            			   long tweetId = database.getTweetId(String.valueOf(tweet.getId()));
	            			   if(!database.isPlebMentionInDB(tweetId, tweet.getUser().getId())){
	            				   database.addPlebTweetMentions(tweetId, database.getVipID(screenName));
	            			   }
	            		   } else {
		            		   	count++;
			                	Tweet plebTweet = new Tweet();
			                	
			                	//auto-inc ID
				            	plebTweet.setAuthorId(tweet.getUser().getId());
				            	plebTweet.setIdStr(String.valueOf(tweet.getId()));
				            	plebTweet.setText(tweet.getText());
				            	plebTweet.setScreenName(tweet.getUser().getScreenName());
				            	

								UserMentionEntity[] userMentionEntities = tweet.getUserMentionEntities();
								Long[] userMentions = new Long[userMentionEntities.length];
								for (int i = 0; i < userMentionEntities.length; i++) {
									long userMention = userMentionEntities[i].getId();
									userMentions[i] = userMention;
								}
								plebTweet.setMentions(userMentions);
				            	database.addPlebTweet(plebTweet, database.getVipID(screenName));
	            		   }
	            	   }
	               }
	           } while ((query = result.nextQuery()) != null && count <= 100);
	           System.out.println("Number of tweets: " + count);
	       } catch (TwitterException te) {
	            te.printStackTrace();
	            System.out.println("Failed to search tweets: " + te.getMessage());
	            System.exit(-1);
	       }
	}
	
	public void crawlPlebFriends() {
		try {
			System.out.println("Start crawling PlebFriends");
			long[] plebIds = database.getRelevantPlebsWithoutFriends();

			//15 getFriends request available
			int tillWait = this.checkRateLimit("/friends/ids");
			for (long plebId : plebIds) {
				Pleb pleb = new Pleb();
				pleb.setId(plebId);
				if (tillWait < 1) {
					System.out.println("Sleep 15 minutes...");
					Thread.sleep(901000);
					tillWait = this.checkRateLimit("/friends/ids");
				}
				System.out.println("add: " + plebId);
				long[] friendList;

				User user = twitter.showUser(plebId);
				if (user.isProtected()) {
					System.out.println("User is protected :(");
				} else {
					friendList = this.getFriendsIDs(user.getScreenName());
					tillWait--;
					Long[] friends = new Long[friendList.length];
					for (int i = 0; i < friendList.length; i++) {
						friends[i] = new Long(friendList[i]);
					}
					pleb.setFriends(friends);
					database.addPlebFriends(pleb);
				}
			}
		} catch (TwitterException | InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public void crawlVipStatus(ArrayList<String> vipNames)
 	{
 		List<List<String>> parts = prepareVipLists(vipNames, 100);
 		
 		for(List<String> part : parts){
 			
 			String[] vipnamesArr = new String[part.size()];
 			vipnamesArr = part.toArray(vipnamesArr);
 			
 			System.out.println("Crawle Userdaten der Vips");
 			this.writeStatus(vipnamesArr);
 			
 		}
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
 	
 	public void writeStatus(String[] screenNames){
 		
        try {
		
 		File yourFile = new File("politiker_status.txt");
 		if(!yourFile.exists()) {
 		    yourFile.createNewFile();
 		} 
 		FileOutputStream oFile = new FileOutputStream(yourFile, false);
 		String content = "";
            ResponseList<User> users = twitter.lookupUsers(screenNames);
            for (User user : users) {
                    System.out.println("@" + user.getScreenName());
                    System.out.println("Status " + user.getStatus());
                    System.out.println("");
                    
                    content += user.getScreenName() + "\n " + user.getStatus() + "\n\n";
            } 
            
			// get the content in bytes
			byte[] contentInBytes = content.getBytes();

			oFile.write(contentInBytes);
			oFile.flush();
			oFile.close();

			System.out.println("Done");
            
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to lookup users: " + te.getMessage());
            System.exit(-1);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}









