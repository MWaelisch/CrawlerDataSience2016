package model;

public class VipTweet extends Tweet{

	private long inReplyTo;
	private long[] mentions;
	private long retweetOrigin;

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
