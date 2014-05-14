package webfootprint.engine.lda;

public class WikipediaSetting {
	
	private int queryTopNDoc;
	private int delay;
	private boolean parentCate; 
	private boolean interCate;
	private int parentCateLevel;
	
	private int DEFAULT_QUERY_TOP_N_DOC = 3;
	private int DEFAULT_DELAY = 50;
	private boolean DEFAULT_PARENT_CATE = false;
	private boolean DEFAULT_INTER_CATE = true;
	private int DEFAULT_PARENT_CATE_LEVEL = 2;
	
	public WikipediaSetting() {
		this.queryTopNDoc = this.DEFAULT_QUERY_TOP_N_DOC;
		this.delay = this.DEFAULT_DELAY;
		this.parentCate = this.DEFAULT_PARENT_CATE;
		this.interCate = this.DEFAULT_INTER_CATE;
		this.parentCateLevel = this.DEFAULT_PARENT_CATE_LEVEL;
	}
	
	public void setQueryTopNDoc(int queryTopNDoc) {
		this.queryTopNDoc = queryTopNDoc;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public void setParentCate(boolean parentCate) {
		this.parentCate = parentCate;
	}
	
	public void setInterCate(boolean interCate) {
		this.interCate = interCate;
	}
	
	public void setParentCateLevel(int parentCateLevel) {
		this.parentCateLevel = parentCateLevel;
	}
	
	public void setDefaultQueryTopNDoc(int queryTopNDoc) {
		this.DEFAULT_QUERY_TOP_N_DOC = queryTopNDoc;
	}
	
	public void setDefaultDelay(int delay) {
		this.DEFAULT_DELAY = delay;
	}
	
	public void setDefaultParentCate(boolean parentCate) {
		this.DEFAULT_PARENT_CATE = parentCate;
	}
	
	public void setDefaultInterCate(boolean interCate) {
		this.DEFAULT_INTER_CATE = interCate;
	}
	
	public void setDefaultParentCateLevel(int parentCateLevel) {
		this.DEFAULT_PARENT_CATE_LEVEL = parentCateLevel;
	}
	
	public int getQueryTopNDoc() {
		return this.queryTopNDoc;
	}
	
	public int getDelay() {
		return this.delay;
	}
	
	public boolean getParentCate() {
		return this.parentCate;
	}
	
	public boolean getInterCate() {
		return this.interCate;
	}
	
	public int getParentCateLevel() {
		return this.parentCateLevel;
	}
	
}
