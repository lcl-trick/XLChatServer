package cn.xiaolus.xlchat.server.util;

public class XCUserStateMessage extends XCMessage {
	private static final long serialVersionUID = 2308566134889508488L;
	
	protected boolean userOnline;

	public boolean isUserOnline() {
		return userOnline;
	}

	public void setUserOnline(boolean userOnline) {
		this.userOnline = userOnline;
	}

}
