package model;

import java.util.ArrayList;


public class Pleb {

	private long id;
	private Long[] friends;
	private ArrayList<Tweet> tweets;



	public Pleb(){}

	public void addTweet(Tweet tweet){
		if(this.tweets != null){
			this.tweets.add(tweet);
		}else{
			this.tweets = new ArrayList<>();
			this.tweets.add(tweet);
		}

	};
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Long[] getFriends() {
		return friends;
	}
	public void setFriends(Long[] friends) {
		this.friends = friends;
	}
	public ArrayList<Tweet> getTweets() {
		return tweets;
	}
	public void setTweets(ArrayList<Tweet> tweets) {
		this.tweets = tweets;
	}	

}
