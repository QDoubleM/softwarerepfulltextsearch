package Utils;

public class TableColumns {
	private String name;
	private String text;
	private Boolean isAnalyzed;
	private String content;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public Boolean getIsAnalyzed() {
		return isAnalyzed;
	}
	public void setIsAnalyzed(Boolean isAnalyzed) {
		this.isAnalyzed = isAnalyzed;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
