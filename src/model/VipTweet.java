package model;

public class VipTweet {
	
	private int authorId;
	private String authorName;
	private String idStr;
	private int inReplyTo;
	private int[] mentions;
	private int retweetOrigin;
	private String text;
	
	
	
	public int getAuthorId() {
		return authorId;
	}
	public void setAuthorId(int authorId) {
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
	public int getInReplyTo() {
		return inReplyTo;
	}
	public void setInReplyTo(int inReplyTo) {
		this.inReplyTo = inReplyTo;
	}
	public int[] getMentions() {
		return mentions;
	}
	public void setMentions(int[] mentions) {
		this.mentions = mentions;
	}
	public int getRetweetOrigin() {
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
