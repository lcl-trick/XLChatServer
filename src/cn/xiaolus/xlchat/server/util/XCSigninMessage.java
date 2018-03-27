package cn.xiaolus.xlchat.server.util;

public class XCSigninMessage extends XCMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6375555371279883352L;
	
	protected String password;
	
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
