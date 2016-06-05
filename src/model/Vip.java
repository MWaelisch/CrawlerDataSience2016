package model;

import java.util.ArrayList;


//todo make extends from Pleb
public class Vip {
	
	private long id;
	private String screenName;
	private String UserName;
	private int followerCount;
	private long[] friends;
	private String profilePicture;
	private ArrayList<VipTweet> tweets; 
	
	public Vip(){}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	public long[] getFriends() {
		return friends;
	}

	public void setFriends(long[] friends) {
		this.friends = friends;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public ArrayList<VipTweet> getTweets() {
		return tweets;
	}

	public void setTweets(ArrayList<VipTweet> tweets) {
		this.tweets = tweets;
	}	
	
	

}
