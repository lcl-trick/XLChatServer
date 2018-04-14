package cn.xiaolus.xlchat.util;

/**
 * 功能：
 * 用户状态消息
 * 
 * @author 小路
 *
 */
public class XCUserStateMessage extends XCMessage {
	private static final long serialVersionUID = 2308566134889508488L;
//	用户是上线还是下线
	protected boolean userOnline;

	public boolean isUserOnline() {
		return userOnline;
	}

	public void setUserOnline(boolean userOnline) {
		this.userOnline = userOnline;
	}

}
