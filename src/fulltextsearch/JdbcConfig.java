package fulltextsearch;

public class JdbcConfig {
	private String url;
	// 用户名。
	private String userName;
	// 密码。
	private String password;
	// 驱动器的名称。
	private String driverName;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String toString() {
		return this.getClass().getName() + "{driverName:" + driverName+ ", url:" + url + ",userName :" + userName + "}";
	}
}
