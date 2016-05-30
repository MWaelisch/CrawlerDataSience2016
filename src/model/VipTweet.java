package model;

public class VipTweet extends Tweet{

	//todo authorId sometimes also reflects the tweet Id
	private long inReplyTo;
	private long[] mentions;
	private long retweetOrigin;


	public VipTweet(){}

	public VipTweet(long authorId, String text){
		super(authorId, text);
	}

	//for debugging
	public VipTweet(long authorId, String text, int posSentiment){
		super(authorId, text, posSentiment);
	}

	public long getInReplyTo() {
		return inReplyTo;
	}
	public void setInReplyTo(long inReplyTo) {
		this.inReplyTo = inReplyTo;
	}
	public long[] getMentions() {
		return mentions;
	}
	public void setMentions(long[] mentions) {
		this.mentions = mentions;
	}
	public long getRetweetOrigin() {
		return retweetOrigin;
	}
	public void setRetweetOrigin(long retweetOrigin) {
		this.retweetOrigin = retweetOrigin;
	}
}
