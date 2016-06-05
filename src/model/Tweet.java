package model;

public class Tweet {

	private long authorId;
	private String screenName;
	private String idStr;
	private String text;
	private int generatedId;
	private int sentimentPos;
	private int sentimentNeg;
	private Long[] mentions; //todo check if change long->Long had some negative effects in crawling

	public Tweet(){}

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
		if(sentimentPos > Math.abs(sentimentNeg)){
			return sentimentPos^2;
		}else if (sentimentPos < Math.abs(sentimentNeg)){
			return sentimentNeg^2;
		}else{
			return 1;
		}
	}

	public void setSentiScore(Integer[] sentiScore){
		this.sentimentPos = sentiScore[0];
		this.sentimentNeg = sentiScore[1];
	}

	public Long[] getMentions() {
		return mentions;
	}
	public void setMentions(Long[] mentions) {
		this.mentions = mentions;
	}


	//TODO can this be deleted ?
	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

}
