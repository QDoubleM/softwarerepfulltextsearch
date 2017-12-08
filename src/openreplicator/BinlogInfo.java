package openreplicator;

public class BinlogInfo {
	private String binlogName;
    public String getBinlogName() {
		return binlogName;
	}
	public void setBinlogName(String binlogName) {
		this.binlogName = binlogName;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	private Long fileSize;
}
