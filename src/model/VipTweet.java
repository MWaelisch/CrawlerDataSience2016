package model;

public class VipTweet extends Tweet{

	private long inReplyTo;

	private long retweetOrigin;

	public long getInReplyTo() {
		return inReplyTo;
	}
	public void setInReplyTo(long inReplyTo) {
		this.inReplyTo = inReplyTo;
	}
	public long getRetweetOrigin() {
		return retweetOrigin;
	}
	public void setRetweetOrigin(long retweetOrigin) {
		this.retweetOrigin = retweetOrigin;
	}
}
