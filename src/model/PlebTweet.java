package model;

public class PlebTweet {
	private long authorId;
	private String idStr;
    private String tweet;
    private String screenName;
    private int generatedId;
    
    public PlebTweet(){}
    
    public PlebTweet(long authorId, String screenName){
    	this.authorId = authorId;
    	this.screenName = screenName;
    }

    public long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(long authorId) {
		this.authorId = authorId;
	}

	public String getTweet() {
		return tweet;
	}

	public void setTweet(String tweet) {
		this.tweet = tweet;
	}

	public String getIdStr() {
		return idStr;
	}

	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	public int getGeneratedId() {
		return generatedId;
	}
	
	public void setGeneratedId(int generatedId) {
		this.generatedId = generatedId;
	}	
}
