package model;

public class Vip {
	
	private long id;
	private String atName;
	private String UserName;
	private int followerCount;
	private long[] friends;
	
	public Vip(){}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAtName() {
		return atName;
	}

	public void setAtName(String atName) {
		this.atName = atName;
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
	
	public String getFriendsAsString(){
		String friendlist = "";
		for(long id : this.friends){
			friendlist+= id + ";";
		}
		return friendlist;
	}
	
	

}
