package model;

public class PlebTweetMention {
	private long plebTweetId;
	private long mention;

	public PlebTweetMention(){}

	public long getPlebTweetId() {
		return plebTweetId;
	}

	public void setPlebTweetId(int plebTweetId) {
		this.plebTweetId = plebTweetId;
	}

	public long getMention() {
		return mention;
	}

	public void setMention(int mention) {
		this.mention = mention;
	}
}
