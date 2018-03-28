package cn.xiaolus.xlchat.util;

public class XCSignupMessage extends XCMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8429862001867074251L;
	
	protected String name;
	protected String password;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public boolean isPublicMessage() {
		return false;
	}
}
