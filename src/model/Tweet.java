package model;

public class Tweet {
	//todo authorId sometimes also reflects the tweet Id
	private long authorId;
	private String screenName;
	private String idStr;
	private String text;
	private int generatedId;
	private int sentimentPos;
	private int sentimentNeg;
	
	public Tweet(){}
	
	public Tweet(long authorId, String idStr, String text, int generatedId, int sentimentPos, int sentimentNeg){
    	this.authorId = authorId;
    	this.idStr = idStr;
    	this.text = text;
    	this.generatedId = generatedId;
    	this.sentimentPos = sentimentPos;
    	this.sentimentNeg = sentimentNeg;
    }
	
	public Tweet(long authorId, String text) {
		this.authorId = authorId;
		this.text = text;
	}

	public Tweet(long authorId, String text, int posSentiment) {
		this.authorId = authorId;
		this.text = text;
		this.sentimentPos = posSentiment;
	}

	public long getAuthorId() {
		return authorId;
	}
	public void setAuthorId(long authorId) {
		this.authorId = authorId;
	}
	public String getIdStr() {
		return idStr;
	}
	public void setIdStr(String idStr) {
		this.idStr = idStr;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getGeneratedId() {
		return generatedId;
	}
	public void setGeneratedId(int generatedId) {
		this.generatedId = generatedId;
	}
	public int getSentimentPos() {
		return sentimentPos;
	}
	public void setSentimentPos(int sentimentPos) {
		this.sentimentPos = sentimentPos;
	}
	public int getSentimentNeg() {
		return sentimentNeg;
	}
	public void setSentimentNeg(int sentimentNeg) {
		this.sentimentNeg = sentimentNeg;
	}
	public int getSentiment(){
		if(sentimentPos >= Math.abs(sentimentNeg)){
			return sentimentPos;
		}else{
			return sentimentNeg;
		}
	}

	public void setSentiScore(Integer[] sentiScore){
		this.sentimentPos = sentiScore[0];
		this.sentimentNeg = sentiScore[1];
	}

	//TODO can this be deleted ?
	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

}
