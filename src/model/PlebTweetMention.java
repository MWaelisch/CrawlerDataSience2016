package model;

public class PlebTweetMention {
	private int plebTweetId;
	private int mention;

	public PlebTweetMention(){}

	public int getPlebTweetId() {
		return plebTweetId;
	}

	public void setPlebTweetId(int plebTweetId) {
		this.plebTweetId = plebTweetId;
	}

	public int getMention() {
		return mention;
	}

	public void setMention(int mention) {
		this.mention = mention;
	}
}
