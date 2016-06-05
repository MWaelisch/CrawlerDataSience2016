package crawl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import model.PlebFriend;
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
				System.out.println("Limit " + limit);
				return limit;
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
			  
	}
	
	
	public void crawlVips(ArrayList<String> vipNames)
	{
		List<List<String>> parts = prepareVipLists(vipNames, 100);
		
	//	Database database = new Database();
		
		for(List<String> part : parts){
			
			String[] vipnamesArr = new String[part.size()];
			vipnamesArr = part.toArray(vipnamesArr);
			
			System.out.println("Crawle Userdaten der Vips");
			ArrayList<Vip> vips = this.lookupUsers(vipnamesArr);
			
			System.out.println("Userdaten gecrawled");
			
			System.out.println("Crawle Friends der Vips");
			for(Vip vip : vips){
				if(this.checkRateLimit("/friends/ids") <= 1){
					try {
						System.out.println("Sleep 15 minutes...");
						Thread.sleep(901000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
		//				database.closeConnection();
					}
				}
					System.out.println("Crawle Friends von " + vip.getScreenName());
					long[] friends = this.getFriendsIDs(vip.getScreenName());
					vip.setFriends(friends);
					database.addVip(vip);
			}
			System.out.println("Friends gecrawled");
			
		}
	//	database.closeConnection();
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
//                    System.out.println("Profilepicturemini " + user.getMiniProfileImageURL());
//                    System.out.println("Profilepicture bigger" + user.getBiggerProfileImageURL());
//                    System.out.println("Profilepicture" + user.getOriginalProfileImageURL());
                    System.out.println("Profilepicture" + user.getProfileImageURL());
                  
                    
                    
                    Vip vip = new Vip();
                    vip.setId(user.getId());
                    vip.setScreenName(user.getScreenName());
                    vip.setUserName(user.getName());
                    vip.setFollowerCount(user.getFollowersCount());
                    vip.setProfilePicture(user.getProfileImageURL());
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
				System.out.println("Crawl Tweets for " + vip);
				Paging page = new Paging(1, 200);// page number, number per page
				for (int i = 1; i <= 5; i++) { //debug config -> get only 1 page
					if(this.checkRateLimit("/statuses/user_timeline") == 0){
							System.out.println("Sleep 15 minutes...");
							Thread.sleep(901000);
					}
					page.setPage(i);
					if (i == 1) {
						statuses = twitter.getUserTimeline(vip, page);
					} else {
						statuses.addAll(twitter.getUserTimeline(vip, page));
					}
				}
				System.out.println("Finished crawling Tweets for " + vip);
				
				System.out.println("Saving Tweets for " + vip);
	//			Database database = new Database();
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
						System.out.println(userMentionEntities[i].getScreenName());
						userMentions[i] = userMention;
					}
					vipTweet.setMentions(userMentions);
					vipTweet.setText(status.getText());
					if(status.getRetweetedStatus() != null){
						vipTweet.setRetweetOrigin(status.getRetweetedStatus().getUser().getId());
//						System.out.println("Retweeted Text" + status.getRetweetedStatus().getText());
//						System.out.println("Origin Text " + status.getRetweetedStatus().getText());
//						System.out.println("Retweet Origin:" + status.getRetweetedStatus().getUser().getId());
//						System.out.println("Retweet Origin Name: " +  status.getRetweetedStatus().getUser().getName());		
					}
//					if(status.getInReplyToUserId() > 1000){
//						System.out.println("In Reply to ID:" + status.getInReplyToUserId());
//						System.out.println("In Reply to Name " + status.getInReplyToScreenName());	
//					}
					database.addVipTweet(vipTweet);
				}
				System.out.println("Finished Saving Tweets for " + vip);
				//	database.closeConnection();
			}

		} catch (TwitterException | InterruptedException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		}

	}
	

	public void searchTweets(){
		System.out.println("Search for mentions of VIPs");
//		ArrayList<Tweet> pt = new ArrayList<Tweet>();
		
		//debug
//		database.executeQuery("SELECT * FROM PlebTweets");
//		database.closeConnection();
//		System.exit(0);
		
		ArrayList<Vip> vips = database.getAllVIPsfromDB();
		
		for(Vip vip : vips){
			String q = "@" + vip.getScreenName() + " OR #" + vip.getScreenName() + " OR \"" + vip.getUserName() + "\"";
			
			System.out.println("### vipname-query: " + q);
			
			searchTweet(q, vip.getScreenName());
		}
		
		System.out.println("Finished search for mentions of VIPs");
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
	            	   System.out.println("Language: " + tweet.getLang() + "   " + tweet.getText());
	            	   if(tweet.getLang().matches("de|en")){
	            		   if(database.isPlebTweetInDB(String.valueOf(tweet.getId()))){
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
									System.out.println(userMentionEntities[i].getScreenName());
									userMentions[i] = userMention;
								}
								plebTweet.setMentions(userMentions);
				            	
				            	//pt.add(plebTweet);
				            	database.addPlebTweet(plebTweet, database.getVipID(screenName));
	            		   }
	            	   }
	               }
	           } while ((query = result.nextQuery()) != null /*&& count <= 100*/);
	           System.out.println("Number of tweets: " + count);
	       } catch (TwitterException te) {
	            te.printStackTrace();
	            System.out.println("Failed to search tweets: " + te.getMessage());
	            System.exit(-1);
	       }
//		return pt;
	}
	
	public void crawlPlebFriends(int start_incl, int end_excl){
		System.out.println("Start crawling PlebFriends");
		//crawlPlebFriends(pt.get(0));
		
		ArrayList<Tweet> pts = database.getAllTweetsfromDB("plebTweets");
		System.out.println("tocrawl (whole): " + pts.size());
		List<Tweet> pts_l = pts.subList(start_incl, end_excl);

		int cnt_protected = 0;
		int cnt_repeats = 0;
		
		int tillWait = 15;

        int leftToShow = this.checkRateLimit("/users/show/:id");
		for(Tweet pt : pts_l){
			if(!database.isIDInDB(pt.getAuthorId(), "pleb", "plebFriends")){
				
				/*try {
					RateLimitStatus status = twitter.getRateLimitStatus("friends").get("/friends/ids");
					System.out.println("remaining: " + status.getRemaining() + "; reset in: " + status.getSecondsUntilReset());
					if(status.getRemaining() < 1){
						Thread.sleep(status.getSecondsUntilReset() * 1000);
					}
				} catch (TwitterException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				/*if(this.checkRateLimit("/friends/ids") == 0){
					try {
						System.out.println("Sleep 15 minutes...");
						Thread.sleep(901000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
		//				database.closeConnection();
					}
				}*/
				if(tillWait < 1){
					try {
						System.out.println("Sleep 15 minutes...");
						Thread.sleep(901000);
						tillWait = 15;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
		//				database.closeConnection();
					}
				}
				
				System.out.println("add: " + pt.getAuthorId());
				long[] friendList;
				try {
					if(leftToShow <= 1){
						leftToShow = this.checkRateLimit("/users/show/:id");
						if(leftToShow <= 1){
							try {
								System.out.println("Sleep 15 minutes...");
								Thread.sleep(901000);
								leftToShow = this.checkRateLimit("/users/show/:id");
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
				//				database.closeConnection();
							}
						}
					}
					User user = twitter.showUser(pt.getAuthorId());
					leftToShow--;
					if(user.isProtected()){
						System.out.println("User is protected :(");
						cnt_protected++;
						database.cleanProtectedPleb(pt.getGeneratedId());
					} else {
						friendList = this.getFriendsIDs(user.getScreenName());
						tillWait--;
						boolean hasVipFriend = false;
						for(long friend : friendList){
//				    		if(database.isIDInDB(friend, "id", "vip")){
				            	PlebFriend plebFriend = new PlebFriend();
				            	plebFriend.setId(pt.getAuthorId());
				        		plebFriend.setFriend(friend);
				           		database.addPlebFriend(plebFriend);
				           		hasVipFriend = true;
//				    		}
				    	}
//						if(!hasVipFriend){
//							PlebFriend plebFriend = new PlebFriend();
//			            	plebFriend.setId(pt.getAuthorId());
//			        		plebFriend.setFriend(0);
//			           		database.addPlebFriend(plebFriend);
//						}
					}
				} catch (TwitterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
			} else { 
				System.out.println("Pleb was in DB :" + pt.getAuthorId()); 
				cnt_repeats++; 
			}
		}
		
		System.out.println("Finished crawling PlebFriends " + start_incl + " to " + (end_excl-1)
				+"\n -- with " + cnt_repeats + " repeats and " + cnt_protected + "protected accounts");
		//debug
		//database.executeQuery("SELECT * FROM plebTweets;");
	//	database.closeConnection();
	}

}
