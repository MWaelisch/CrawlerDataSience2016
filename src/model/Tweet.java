package model;

import java.io.Serializable;

import static java.lang.Math.sqrt;

public class Tweet implements Serializable{

	private long authorId;
	private String screenName;
	private String idStr;
	private String text;
	private int generatedId;
	private int sentimentPos;
	private int sentimentNeg;
	private Long[] mentions;

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

	public int getSentiment(boolean beef){ //TODO make so that right sentiment is given depending on bff or beef map
		if(beef){
			return sentimentNeg*sentimentNeg;
		}else{
			return sentimentPos * sentimentPos;
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


	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

}
