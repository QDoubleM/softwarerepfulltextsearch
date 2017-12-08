package openreplicator;

public class BinlogMasterStatus {
	private String binlogName;
    public String getBinlogName() {
		return binlogName;
	}
	public void setBinlogName(String binlogName) {
		this.binlogName = binlogName;
	}
	public long getPosition() {
		return position;
	}
	public void setPosition(long position) {
		this.position = position;
	}
	private long position;
}
