package model;

public class VipTweet {
	
	private long authorId;
	private String authorName;
	private String idStr;
	private long inReplyTo;
	private long[] mentions;
	private long retweetOrigin;
	private String text;
	
	
	
	public long getAuthorId() {
		return authorId;
	}
	public void setAuthorId(long authorId) {
		this.authorId = authorId;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getIdStr() {
		return idStr;
	}
	public void setIdStr(String idStr) {
		this.idStr = idStr;
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
	public void setRetweetOrigin(int retweetOrigin) {
		this.retweetOrigin = retweetOrigin;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	

}
