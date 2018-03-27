package cn.xiaolus.xlchat.server.util;

public class XCSignupMessage extends XCMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8429862001867074251L;
	
	protected String user;
	protected String name;
	protected String passwordHash;
	protected String salt;
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	@Override
	public boolean isPublicMessage() {
		return false;
	}
}
